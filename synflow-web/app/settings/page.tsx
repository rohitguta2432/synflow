"use client";

import AppShell from "@/components/AppShell";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { User } from "@/lib/types";
import { isAdmin, getUser } from "@/lib/auth";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Plus, Trash2, Shield } from "lucide-react";
import clsx from "clsx";

export default function SettingsPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({ email: "", password: "", fullName: "", role: "INTERNAL_USER" });

  useEffect(() => {
    const user = getUser();
    if (!user || user.role !== "ADMIN") {
      router.push("/dashboard");
    } else {
      setCurrentUser(user);
    }
  }, [router]);

  const { data: users, isLoading } = useQuery<User[]>({
    queryKey: ["admin-users"],
    queryFn: () => api.get("/api/admin/users").then((r) => r.data),
    enabled: !!currentUser,
  });

  const createMutation = useMutation({
    mutationFn: () => api.post("/api/admin/users", form),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ["admin-users"] }); setShowModal(false); setForm({ email: "", password: "", fullName: "", role: "INTERNAL_USER" }); },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: string) => api.delete(`/api/admin/users/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["admin-users"] }),
  });

  if (!currentUser) return null;

  return (
    <AppShell>
      <div className="space-y-8 max-w-5xl mx-auto">
        <div>
          <h1 className="text-3xl font-extrabold text-primary tracking-tight mb-2">Admin Settings</h1>
          <p className="text-text-secondary">Control platform access and internal user roles.</p>
        </div>

        <div className="grid grid-cols-12 gap-6">
          <div className="col-span-4 space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm">
              <h2 className="text-sm font-bold text-primary uppercase tracking-wider mb-6">Access Overview</h2>
              <div className="space-y-3">
                <div className="flex justify-between items-center p-3 bg-surface rounded-lg">
                  <span className="text-sm text-text-secondary">Total Users</span>
                  <span className="text-xl font-bold text-primary">{users?.length ?? 0}</span>
                </div>
                <div className="flex justify-between items-center p-3 bg-surface rounded-lg">
                  <span className="text-sm text-text-secondary">Admins</span>
                  <span className="text-xl font-bold text-accent">{users?.filter(u => u.role === "ADMIN").length ?? 0}</span>
                </div>
              </div>
              <button onClick={() => setShowModal(true)}
                className="w-full mt-6 flex items-center justify-center gap-2 bg-primary text-white py-3 rounded-lg font-bold hover:bg-primary/90 transition-colors">
                <Plus className="w-4 h-4" /> Invite New User
              </button>
            </div>
          </div>

          <div className="col-span-8">
            <div className="bg-white rounded-xl shadow-sm overflow-hidden">
              <div className="px-6 py-4 border-b border-border">
                <h2 className="font-bold text-primary">Active Team Members</h2>
              </div>
              <table className="w-full text-left">
                <thead className="bg-surface">
                  <tr>
                    <th className="px-6 py-3 text-[10px] font-bold text-text-secondary uppercase tracking-widest">User</th>
                    <th className="px-6 py-3 text-[10px] font-bold text-text-secondary uppercase tracking-widest">Role</th>
                    <th className="px-6 py-3 text-[10px] font-bold text-text-secondary uppercase tracking-widest text-right">Actions</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border/50">
                  {users?.map(user => (
                    <tr key={user.id} className="hover:bg-surface/50 transition-colors">
                      <td className="px-6 py-4">
                        <div className="flex items-center gap-3">
                          <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center font-bold text-primary text-xs">
                            {user.fullName.split(" ").map(w => w[0]).join("").slice(0,2)}
                          </div>
                          <div>
                            <div className="text-sm font-bold text-primary">{user.fullName}</div>
                            <div className="text-xs text-text-secondary">{user.email}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4">
                        <span className={clsx("px-2.5 py-1 text-[11px] font-bold rounded",
                          user.role === "ADMIN" ? "bg-primary/10 text-primary" : "bg-surface text-text-secondary"
                        )}>{user.role === "ADMIN" ? "Admin" : "Analyst"}</span>
                      </td>
                      <td className="px-6 py-4 text-right">
                        {user.id !== currentUser.id && (
                          <button onClick={() => { if(confirm(`Delete ${user.fullName}?`)) deleteMutation.mutate(user.id); }}
                            className="text-text-secondary hover:text-error transition-colors">
                            <Trash2 className="w-4 h-4" />
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {isLoading && <div className="p-8 text-center"><div className="animate-spin w-6 h-6 border-2 border-accent border-t-transparent rounded-full mx-auto" /></div>}
            </div>
          </div>
        </div>

        {/* Security Card */}
        <div className="bg-gradient-to-br from-primary to-primary/80 text-white p-8 rounded-xl flex items-center gap-6">
          <Shield className="w-12 h-12 text-accent" />
          <div>
            <h3 className="text-lg font-bold mb-1">Security Notice</h3>
            <p className="text-blue-200 text-sm">All API access is secured with JWT tokens. Sensitive data fields are encrypted with AES-256.</p>
          </div>
        </div>
      </div>

      {/* Create User Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-8 w-full max-w-md shadow-xl">
            <h3 className="text-lg font-bold text-primary mb-6">Create New User</h3>
            <div className="space-y-4">
              <div>
                <label className="text-xs font-bold text-text-secondary block mb-1">Full Name</label>
                <input value={form.fullName} onChange={e => setForm({ ...form, fullName: e.target.value })}
                  className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
              </div>
              <div>
                <label className="text-xs font-bold text-text-secondary block mb-1">Email</label>
                <input type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}
                  className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
              </div>
              <div>
                <label className="text-xs font-bold text-text-secondary block mb-1">Password</label>
                <input type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })}
                  className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
              </div>
              <div>
                <label className="text-xs font-bold text-text-secondary block mb-1">Role</label>
                <select value={form.role} onChange={e => setForm({ ...form, role: e.target.value })}
                  className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent">
                  <option value="INTERNAL_USER">Analyst</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
            </div>
            <div className="flex justify-end gap-3 mt-6">
              <button onClick={() => setShowModal(false)} className="px-4 py-2 text-sm font-bold text-text-secondary hover:bg-surface rounded-lg">Cancel</button>
              <button onClick={() => createMutation.mutate()} disabled={createMutation.isPending || !form.email || !form.password || !form.fullName}
                className="px-6 py-2 text-sm font-bold text-white bg-accent rounded-lg hover:bg-sky-600 disabled:opacity-50">
                {createMutation.isPending ? "Creating..." : "Create User"}
              </button>
            </div>
            {createMutation.isError && <p className="text-error text-xs mt-3">Failed to create user. Email may already exist.</p>}
          </div>
        </div>
      )}
    </AppShell>
  );
}
