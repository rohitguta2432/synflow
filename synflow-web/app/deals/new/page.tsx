"use client";

import AppShell from "@/components/AppShell";
import DealForm from "@/components/DealForm";
import { useMutation } from "@tanstack/react-query";
import api from "@/lib/api";
import { useRouter } from "next/navigation";

export default function NewDealPage() {
  const router = useRouter();
  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => api.post("/api/deals", data),
    onSuccess: (res) => router.push(`/deals/${res.data.id}`),
  });

  return (
    <AppShell>
      <h1 className="text-3xl font-extrabold text-primary tracking-tight mb-2">Create Deal</h1>
      <p className="text-text-secondary mb-8">Add a new transaction to the pipeline.</p>
      <DealForm onSubmit={(data) => mutation.mutate(data)} loading={mutation.isPending} />
    </AppShell>
  );
}
