"use client";

import AppShell from "@/components/AppShell";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { Deal, Match } from "@/lib/types";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { Pencil, Trash2, Zap, ArrowLeft } from "lucide-react";
import clsx from "clsx";
import { isAdmin } from "@/lib/auth";

export default function DealDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const queryClient = useQueryClient();

  const { data: deal, isLoading } = useQuery<Deal>({
    queryKey: ["deal", id],
    queryFn: () => api.get(`/api/deals/${id}`).then((r) => r.data),
  });

  const matchMutation = useMutation({
    mutationFn: () => api.post(`/api/deals/${id}/match`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["deal", id] }),
  });

  const deleteMutation = useMutation({
    mutationFn: () => api.delete(`/api/deals/${id}`),
    onSuccess: () => router.push("/deals"),
  });

  if (isLoading) return <AppShell><div className="flex justify-center py-20"><div className="animate-spin w-8 h-8 border-4 border-accent border-t-transparent rounded-full" /></div></AppShell>;
  if (!deal) return <AppShell><p className="text-center py-20 text-text-secondary">Deal not found</p></AppShell>;

  return (
    <AppShell>
      <div className="space-y-8">
        <div className="flex items-center gap-3 mb-2">
          <Link href="/deals" className="text-text-secondary hover:text-primary"><ArrowLeft className="w-5 h-5" /></Link>
          <span className="text-xs text-text-secondary uppercase tracking-widest font-bold">Deals / {deal.title}</span>
        </div>

        <div className="flex justify-between items-start">
          <div>
            <h2 className="text-3xl font-bold tracking-tight text-primary">{deal.title}</h2>
            <p className="text-text-secondary mt-1">{deal.industry} &middot; {deal.dealType} &middot; {deal.ticketSize}</p>
          </div>
          <div className="flex gap-3">
            <button onClick={() => matchMutation.mutate()} disabled={matchMutation.isPending}
              className="flex items-center gap-2 px-5 py-2.5 bg-accent text-white rounded-lg text-sm font-bold hover:bg-sky-600 transition-colors disabled:opacity-50">
              {matchMutation.isPending ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : <Zap className="w-4 h-4" />}
              Run Matching
            </button>
            <Link href={`/deals/${id}/edit`} className="flex items-center gap-1.5 px-4 py-2 border border-border rounded-lg text-sm font-medium hover:bg-surface transition-colors">
              <Pencil className="w-3.5 h-3.5" /> Edit
            </Link>
            {isAdmin() && (
              <button onClick={() => { if(confirm("Delete this deal?")) deleteMutation.mutate(); }}
                className="flex items-center gap-1.5 px-4 py-2 border border-error/20 text-error rounded-lg text-sm font-medium hover:bg-red-50 transition-colors">
                <Trash2 className="w-3.5 h-3.5" /> Delete
              </button>
            )}
          </div>
        </div>

        <div className="grid grid-cols-12 gap-6">
          <div className="col-span-8 bg-white p-6 rounded-xl shadow-sm">
            <h3 className="text-sm font-bold text-text-secondary uppercase tracking-wider mb-4">Deal Specification</h3>
            <div className="grid grid-cols-3 gap-6 mb-6">
              <div><p className="text-[10px] text-text-secondary uppercase font-bold mb-1">Industry</p><p className="text-sm font-semibold text-primary">{deal.industry}</p></div>
              <div><p className="text-[10px] text-text-secondary uppercase font-bold mb-1">Type</p><p className="text-sm font-semibold text-primary">{deal.dealType}</p></div>
              <div><p className="text-[10px] text-text-secondary uppercase font-bold mb-1">Ticket Size</p><p className="text-sm font-semibold text-primary">{deal.ticketSize}</p></div>
            </div>
            <div className="mb-6">
              <p className="text-[10px] text-text-secondary uppercase font-bold mb-1">Geography</p>
              <div className="flex gap-2">{deal.geography?.map(g => <span key={g} className="px-2 py-1 bg-surface text-xs font-medium rounded border border-border">{g}</span>)}</div>
            </div>
            {deal.requirements && (
              <div>
                <p className="text-[10px] text-text-secondary uppercase font-bold mb-1">Requirements</p>
                <p className="text-sm text-text-primary leading-relaxed">{deal.requirements}</p>
              </div>
            )}
          </div>
          <div className="col-span-4 bg-surface p-6 rounded-xl">
            <p className="text-sm font-bold text-text-secondary uppercase tracking-wider mb-2">Status</p>
            <span className={clsx("px-3 py-1 rounded-full text-xs font-bold uppercase", {
              "bg-green-100 text-green-700": deal.status === "ACTIVE",
              "bg-slate-200 text-slate-700": deal.status === "CLOSED",
              "bg-amber-100 text-amber-700": deal.status === "DRAFT",
            })}>{deal.status}</span>
          </div>
        </div>

        {/* Matched Profiles */}
        <div>
          <div className="flex items-center gap-3 mb-4">
            <h3 className="text-xl font-bold text-primary">Matched Profiles</h3>
            {deal.matches && <span className="bg-primary text-white px-2 py-0.5 rounded text-[10px] font-bold">{deal.matches.length} FOUND</span>}
          </div>

          <div className="space-y-3">
            {deal.matches?.map(match => (
              <div key={match.id} className="bg-white rounded-xl p-5 shadow-sm flex items-center justify-between hover:shadow-md transition-shadow cursor-pointer"
                   onClick={() => router.push(`/profiles/${match.profileId}`)}>
                <div className="flex items-center gap-4">
                  <div className="w-10 h-10 rounded-lg bg-primary/10 flex items-center justify-center text-primary font-bold text-xs">
                    {match.profileName.split(" ").map(w => w[0]).join("").slice(0,2)}
                  </div>
                  <div>
                    <p className="text-sm font-bold text-primary">{match.profileName}</p>
                    <div className="flex gap-1 mt-1">
                      {match.profileExpertise?.slice(0,3).map(t => (
                        <span key={t} className="text-[10px] bg-surface text-text-secondary px-1.5 py-0.5 rounded font-medium">{t}</span>
                      ))}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-6">
                  <div className="text-right">
                    <div className="flex items-center gap-2">
                      <div className="w-20 bg-surface h-2 rounded-full overflow-hidden">
                        <div className={clsx("h-full rounded-full", {
                          "bg-success": match.relevanceScore >= 70,
                          "bg-warning": match.relevanceScore >= 40 && match.relevanceScore < 70,
                          "bg-error": match.relevanceScore < 40,
                        })} style={{ width: `${match.relevanceScore}%` }} />
                      </div>
                      <span className="text-lg font-extrabold text-primary">{match.relevanceScore}%</span>
                    </div>
                  </div>
                  <p className="text-xs text-text-secondary max-w-xs">{match.matchReason}</p>
                </div>
              </div>
            ))}
            {(!deal.matches || deal.matches.length === 0) && (
              <div className="bg-white rounded-xl p-8 text-center shadow-sm">
                <p className="text-text-secondary">No matches yet. Click &quot;Run Matching&quot; to find profiles.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </AppShell>
  );
}
