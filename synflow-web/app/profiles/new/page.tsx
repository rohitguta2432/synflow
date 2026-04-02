"use client";

import AppShell from "@/components/AppShell";
import ProfileForm from "@/components/ProfileForm";
import { useMutation } from "@tanstack/react-query";
import api from "@/lib/api";
import { useRouter } from "next/navigation";

export default function NewProfilePage() {
  const router = useRouter();
  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => api.post("/api/profiles", data),
    onSuccess: (res) => router.push(`/profiles/${res.data.id}`),
  });

  return (
    <AppShell>
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl font-extrabold text-primary tracking-tight mb-2">Create Profile</h1>
        <p className="text-text-secondary mb-8">Add a new intelligence entity to the network.</p>
        <ProfileForm onSubmit={(data) => mutation.mutate(data)} loading={mutation.isPending} />
        {mutation.isError && <p className="text-error mt-4 text-sm">Failed to create profile. Please try again.</p>}
      </div>
    </AppShell>
  );
}
