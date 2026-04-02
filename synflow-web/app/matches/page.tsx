"use client";

import AppShell from "@/components/AppShell";
import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Match, Page as PageType } from "@/lib/types";
import { useState } from "react";
import { useRouter } from "next/navigation";
import clsx from "clsx";

export default function MatchesPage() {
  const router = useRouter();
  const [dealId, setDealId] = useState("");
  const [profileId, setProfileId] = useState("");
  const [minScore, setMinScore] = useState(0);
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery<PageType<Match>>({
    queryKey: ["matches", dealId, profileId, minScore, page],
    queryFn: () => {
      const params = new URLSearchParams();
      if (dealId) params.set("dealId", dealId);
      if (profileId) params.set("profileId", profileId);
      if (minScore > 0) params.set("minScore", String(minScore));
      params.set("page", String(page));
      params.set("size", "20");
      params.set("sort", "relevanceScore,desc");
      return api.get(`/api/matches?${params}`).then((r) => r.data);
    },
  });

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight text-primary">Intelligence Matches</h2>
          <p className="text-text-secondary">Neural correlation analysis of market opportunities against enterprise profiles.</p>
        </div>

        <div className="flex items-center gap-4">
          <label className="text-xs font-bold text-text-secondary">Min Score: {minScore}%</label>
          <input type="range" min={0} max={100} value={minScore} onChange={e => { setMinScore(Number(e.target.value)); setPage(0); }}
            className="w-48 accent-accent" />
          <button onClick={() => { setDealId(""); setProfileId(""); setMinScore(0); setPage(0); }}
            className="text-xs font-semibold text-accent hover:underline ml-auto">Reset Filters</button>
        </div>

        <div className="space-y-4">
          {data?.content.map(match => (
            <div key={match.id} className="bg-white rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow">
              <div className="grid grid-cols-12 gap-8 items-center">
                <div className="col-span-4">
                  <div className="flex items-start gap-4">
                    <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center text-primary font-bold">
                      {match.dealTitle.split(" ").map(w => w[0]).join("").slice(0,2)}
                    </div>
                    <div>
                      <h3 className="text-base font-bold text-primary cursor-pointer hover:text-accent" onClick={() => router.push(`/deals/${match.dealId}`)}>
                        {match.dealTitle}
                      </h3>
                      <p className="text-sm text-text-secondary">{match.dealIndustry}</p>
                    </div>
                  </div>
                </div>
                <div className="col-span-4 flex flex-col items-center">
                  <div className="relative w-full flex items-center justify-center py-2">
                    <div className="absolute inset-0 flex items-center"><div className="h-px w-full bg-border" /></div>
                    <div className="relative z-10 bg-white px-4 flex flex-col items-center">
                      <div className={clsx("text-2xl font-black tracking-tighter", {
                        "text-success": match.relevanceScore >= 70,
                        "text-warning": match.relevanceScore >= 40 && match.relevanceScore < 70,
                        "text-error": match.relevanceScore < 40,
                      })}>{match.relevanceScore}%</div>
                      <div className="text-[10px] font-bold uppercase tracking-widest text-text-secondary">Match</div>
                    </div>
                  </div>
                  <p className="text-xs text-text-secondary text-center italic mt-1">{match.matchReason}</p>
                </div>
                <div className="col-span-4 flex items-center justify-end">
                  <div className="flex items-center gap-4 text-right">
                    <div>
                      <h3 className="text-base font-bold text-primary cursor-pointer hover:text-accent" onClick={() => router.push(`/profiles/${match.profileId}`)}>
                        {match.profileName}
                      </h3>
                      <div className="flex gap-1 justify-end mt-1">
                        {match.profileExpertise?.slice(0,2).map(t => (
                          <span key={t} className="text-[10px] bg-surface text-text-secondary px-1.5 py-0.5 rounded">{t}</span>
                        ))}
                      </div>
                    </div>
                    <div className="w-10 h-10 rounded-full bg-accent/10 flex items-center justify-center text-accent font-bold text-xs">
                      {match.profileName.split(" ").map(w => w[0]).join("").slice(0,2)}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          ))}

          {isLoading && <div className="text-center py-12"><div className="animate-spin w-8 h-8 border-4 border-accent border-t-transparent rounded-full mx-auto" /></div>}
          {data && data.content.length === 0 && <div className="bg-white rounded-xl p-12 text-center shadow-sm"><p className="text-text-secondary">No matches found with current filters.</p></div>}
        </div>

        {data && data.totalPages > 1 && (
          <div className="flex justify-center gap-2">
            <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="px-4 py-2 rounded-lg text-sm font-bold border border-border hover:bg-surface disabled:opacity-30">Previous</button>
            <span className="px-4 py-2 text-sm text-text-secondary">Page {page + 1} of {data.totalPages}</span>
            <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)} className="px-4 py-2 rounded-lg text-sm font-bold border border-border hover:bg-surface disabled:opacity-30">Next</button>
          </div>
        )}
      </div>
    </AppShell>
  );
}
