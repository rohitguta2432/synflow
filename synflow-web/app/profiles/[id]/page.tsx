"use client";

import AppShell from "@/components/AppShell";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Profile } from "@/lib/types";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { Pencil, Trash2, Plus, Network, ArrowLeft } from "lucide-react";
import clsx from "clsx";
import { isAdmin } from "@/lib/auth";
import { useState } from "react";

export default function ProfileDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const queryClient = useQueryClient();
  const [showAddConn, setShowAddConn] = useState(false);
  const [connSearch, setConnSearch] = useState("");
  const [connType, setConnType] = useState("business partner");

  const { data: profile, isLoading } = useQuery<Profile>({
    queryKey: ["profile", id],
    queryFn: () => api.get(`/api/profiles/${id}`).then((r) => r.data),
  });

  const { data: searchProfiles } = useQuery({
    queryKey: ["profile-search", connSearch],
    queryFn: () => api.get(`/api/profiles?search=${connSearch}&size=5`).then((r) => r.data),
    enabled: connSearch.length > 1,
  });

  const deleteMutation = useMutation({
    mutationFn: () => api.delete(`/api/profiles/${id}`),
    onSuccess: () => router.push("/profiles"),
  });

  const addConnMutation = useMutation({
    mutationFn: (connId: string) =>
      api.post(`/api/profiles/${id}/connections`, { connectedProfileId: connId, connectionType: connType }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ["profile", id] }); setShowAddConn(false); },
  });

  if (isLoading) return <AppShell><div className="flex justify-center py-20"><div className="animate-spin w-8 h-8 border-4 border-accent border-t-transparent rounded-full" /></div></AppShell>;
  if (!profile) return <AppShell><p className="text-center py-20 text-text-secondary">Profile not found</p></AppShell>;

  return (
    <AppShell>
      <div className="space-y-8">
        <div className="flex items-center gap-3 mb-2">
          <Link href="/profiles" className="text-text-secondary hover:text-primary"><ArrowLeft className="w-5 h-5" /></Link>
          <span className="text-xs text-text-secondary uppercase tracking-widest font-bold">Profiles / {profile.name}</span>
        </div>

        {/* Hero */}
        <div className="grid grid-cols-12 gap-8">
          <div className="col-span-12 lg:col-span-8 space-y-6">
            <div className="bg-white p-8 rounded-xl shadow-sm">
              <div className="flex justify-between items-start mb-6">
                <div className="flex gap-6 items-start">
                  <div className="w-20 h-20 rounded-xl bg-primary/10 flex items-center justify-center text-primary text-2xl font-bold">
                    {profile.name.split(" ").map(w => w[0]).join("").slice(0,2)}
                  </div>
                  <div>
                    <div className="flex items-center gap-3 mb-1">
                      <h1 className="text-2xl font-extrabold text-primary">{profile.name}</h1>
                      <span className="bg-surface text-text-secondary text-xs font-bold px-3 py-1 rounded-full">{profile.uniqueCode}</span>
                      <span className={clsx("px-2.5 py-1 rounded-full text-[10px] font-bold uppercase",
                        profile.type === "REAL" ? "bg-blue-50 text-blue-700" : "bg-slate-100 text-slate-600")}>{profile.type}</span>
                    </div>
                    {profile.summary && <p className="text-text-secondary text-sm mb-3">{profile.summary}</p>}
                    <div className="flex flex-wrap gap-2">
                      {profile.expertise?.map(tag => (
                        <span key={tag} className="bg-primary/5 text-primary border border-primary/10 text-[11px] font-bold px-3 py-1 rounded">{tag}</span>
                      ))}
                    </div>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Link href={`/profiles/${id}/edit`} className="flex items-center gap-1.5 px-4 py-2 border border-border rounded-lg text-sm font-medium hover:bg-surface transition-colors">
                    <Pencil className="w-3.5 h-3.5" /> Edit
                  </Link>
                  {isAdmin() && (
                    <button onClick={() => { if(confirm("Delete this profile?")) deleteMutation.mutate(); }}
                      className="flex items-center gap-1.5 px-4 py-2 border border-error/20 text-error rounded-lg text-sm font-medium hover:bg-red-50 transition-colors">
                      <Trash2 className="w-3.5 h-3.5" /> Delete
                    </button>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-4 gap-6 py-6 border-t border-border">
                <div><p className="text-[10px] uppercase tracking-wider text-text-secondary font-bold mb-1">Industry</p><p className="text-sm font-semibold">{profile.industryFocus}</p></div>
                <div><p className="text-[10px] uppercase tracking-wider text-text-secondary font-bold mb-1">Geography</p><p className="text-sm font-semibold">{profile.geographicReach?.join(", ")}</p></div>
                <div><p className="text-[10px] uppercase tracking-wider text-text-secondary font-bold mb-1">Status</p><p className="text-sm font-semibold">{profile.contactStatus.replace("_", " ")}</p></div>
                <div><p className="text-[10px] uppercase tracking-wider text-text-secondary font-bold mb-1">AI Generated</p><p className="text-sm font-semibold">{profile.aiGenerated ? "Yes" : "No"}</p></div>
              </div>

              {profile.servicesOffered && (
                <div className="py-4 border-t border-border">
                  <p className="text-[10px] uppercase tracking-wider text-text-secondary font-bold mb-2">Services Offered</p>
                  <p className="text-sm text-text-primary">{profile.servicesOffered}</p>
                </div>
              )}
              {profile.trackRecord && (
                <div className="py-4 border-t border-border">
                  <p className="text-[10px] uppercase tracking-wider text-text-secondary font-bold mb-2">Track Record</p>
                  <p className="text-sm text-text-primary">{profile.trackRecord}</p>
                </div>
              )}
            </div>
          </div>

          {/* Connections */}
          <div className="col-span-12 lg:col-span-4 space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-bold text-primary">Connections</h3>
                <button onClick={() => setShowAddConn(!showAddConn)} className="text-accent text-xs font-bold flex items-center gap-1">
                  <Plus className="w-3 h-3" /> Add
                </button>
              </div>

              {showAddConn && (
                <div className="mb-4 p-3 bg-surface rounded-lg space-y-2">
                  <input type="text" placeholder="Search profiles..." value={connSearch} onChange={e => setConnSearch(e.target.value)}
                    className="w-full text-xs bg-white border border-border rounded px-3 py-2 focus:ring-1 focus:ring-accent" />
                  <select value={connType} onChange={e => setConnType(e.target.value)}
                    className="w-full text-xs bg-white border border-border rounded px-3 py-2">
                    <option>business partner</option><option>referral</option><option>advisor</option><option>investor</option>
                  </select>
                  {searchProfiles?.content?.map((p: Profile) => (
                    <button key={p.id} onClick={() => addConnMutation.mutate(p.id)}
                      className="w-full text-left text-xs p-2 hover:bg-white rounded flex justify-between items-center">
                      <span className="font-medium">{p.name}</span>
                      <span className="text-text-secondary">{p.industryFocus}</span>
                    </button>
                  ))}
                </div>
              )}

              <div className="space-y-3">
                {profile.connections?.map(conn => (
                  <Link key={conn.id} href={`/profiles/${conn.connectedProfileId}`}
                    className="block p-3 bg-surface rounded-lg hover:bg-border/30 transition-colors">
                    <p className="text-sm font-bold text-primary">{conn.connectedProfileName}</p>
                    <div className="flex gap-2 mt-1">
                      <span className="text-[10px] text-text-secondary">{conn.connectedProfileIndustry}</span>
                      <span className="text-[10px] text-accent font-medium">{conn.connectionType}</span>
                    </div>
                  </Link>
                ))}
                {(!profile.connections || profile.connections.length === 0) && (
                  <p className="text-text-secondary text-xs text-center py-4">No connections yet</p>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Matched Deals */}
        {profile.matches && profile.matches.length > 0 && (
          <div className="bg-white rounded-xl shadow-sm overflow-hidden">
            <div className="px-6 py-4 border-b border-border">
              <h3 className="font-bold text-primary">Matched Deals</h3>
            </div>
            <table className="w-full text-left">
              <thead><tr className="bg-surface text-[10px] text-text-secondary font-bold uppercase tracking-widest">
                <th className="px-6 py-3">Deal</th><th className="px-6 py-3">Industry</th>
                <th className="px-6 py-3 text-right">Score</th><th className="px-6 py-3">Reason</th>
              </tr></thead>
              <tbody className="divide-y divide-border/50">
                {profile.matches.map(m => (
                  <tr key={m.id} className="hover:bg-surface/50 cursor-pointer" onClick={() => router.push(`/deals/${m.dealId}`)}>
                    <td className="px-6 py-3 text-sm font-bold text-primary">{m.dealTitle}</td>
                    <td className="px-6 py-3 text-sm">{m.dealIndustry}</td>
                    <td className="px-6 py-3 text-right">
                      <span className={clsx("text-xs font-bold px-2 py-1 rounded", {
                        "bg-green-50 text-green-700": m.relevanceScore >= 70,
                        "bg-amber-50 text-amber-700": m.relevanceScore >= 40 && m.relevanceScore < 70,
                        "bg-red-50 text-red-700": m.relevanceScore < 40,
                      })}>{m.relevanceScore}%</span>
                    </td>
                    <td className="px-6 py-3 text-xs text-text-secondary">{m.matchReason}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </AppShell>
  );
}
