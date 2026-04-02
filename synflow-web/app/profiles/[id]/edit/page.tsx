"use client";

import AppShell from "@/components/AppShell";
import ProfileForm from "@/components/ProfileForm";
import { useQuery, useMutation } from "@tanstack/react-query";
import api from "@/lib/api";
import { Profile } from "@/lib/types";
import { useParams, useRouter } from "next/navigation";

export default function EditProfilePage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const { data: profile, isLoading } = useQuery<Profile>({
    queryKey: ["profile", id],
    queryFn: () => api.get(`/api/profiles/${id}`).then((r) => r.data),
  });

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => api.put(`/api/profiles/${id}`, data),
    onSuccess: () => router.push(`/profiles/${id}`),
  });

  if (isLoading) return <AppShell><div className="flex justify-center py-20"><div className="animate-spin w-8 h-8 border-4 border-accent border-t-transparent rounded-full" /></div></AppShell>;
  if (!profile) return <AppShell><p className="text-center py-20 text-text-secondary">Profile not found</p></AppShell>;

  return (
    <AppShell>
      <div className="max-w-5xl mx-auto">
        <h1 className="text-3xl font-extrabold text-primary tracking-tight mb-2">Edit Profile</h1>
        <p className="text-text-secondary mb-8">Update intelligence entity: {profile.name}</p>
        <ProfileForm initial={profile} onSubmit={(data) => mutation.mutate(data)} loading={mutation.isPending} />
      </div>
    </AppShell>
  );
}
