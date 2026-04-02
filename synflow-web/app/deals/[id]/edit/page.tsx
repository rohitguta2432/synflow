"use client";

import AppShell from "@/components/AppShell";
import DealForm from "@/components/DealForm";
import { useQuery, useMutation } from "@tanstack/react-query";
import api from "@/lib/api";
import { Deal } from "@/lib/types";
import { useParams, useRouter } from "next/navigation";

export default function EditDealPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const { data: deal, isLoading } = useQuery<Deal>({
    queryKey: ["deal", id],
    queryFn: () => api.get(`/api/deals/${id}`).then((r) => r.data),
  });

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => api.put(`/api/deals/${id}`, data),
    onSuccess: () => router.push(`/deals/${id}`),
  });

  if (isLoading) return <AppShell><div className="flex justify-center py-20"><div className="animate-spin w-8 h-8 border-4 border-accent border-t-transparent rounded-full" /></div></AppShell>;
  if (!deal) return <AppShell><p className="text-center py-20 text-text-secondary">Deal not found</p></AppShell>;

  return (
    <AppShell>
      <h1 className="text-3xl font-extrabold text-primary tracking-tight mb-2">Edit Deal</h1>
      <p className="text-text-secondary mb-8">Update: {deal.title}</p>
      <DealForm initial={deal} onSubmit={(data) => mutation.mutate(data)} loading={mutation.isPending} />
    </AppShell>
  );
}
