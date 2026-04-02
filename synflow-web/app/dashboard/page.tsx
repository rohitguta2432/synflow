"use client";

import AppShell from "@/components/AppShell";
import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Stats, Deal, Match, Page } from "@/lib/types";
import Link from "next/link";
import { Users, Handshake, Sparkles, TrendingUp, ArrowRight } from "lucide-react";
import clsx from "clsx";

export default function DashboardPage() {
  const { data: stats } = useQuery<Stats>({
    queryKey: ["stats"],
    queryFn: () => api.get("/api/matches/stats").then((r) => r.data),
  });

  const { data: recentDeals } = useQuery<Page<Deal>>({
    queryKey: ["recent-deals"],
    queryFn: () => api.get("/api/deals?size=5&sort=createdAt,desc").then((r) => r.data),
  });

  const { data: recentMatches } = useQuery<Page<Match>>({
    queryKey: ["recent-matches"],
    queryFn: () => api.get("/api/matches?size=5&sort=matchedAt,desc").then((r) => r.data),
  });

  const statCards = [
    { label: "Total Profiles", value: stats?.totalProfiles ?? 0, icon: Users, color: "text-primary" },
    { label: "Active Deals", value: stats?.totalDeals ?? 0, icon: Handshake, color: "text-accent" },
    { label: "Total Matches", value: stats?.totalMatches ?? 0, icon: Sparkles, color: "text-primary" },
  ];

  return (
    <AppShell>
      <div className="space-y-8">
        <div className="flex justify-between items-end">
          <div>
            <h2 className="text-3xl font-extrabold text-primary tracking-tight">Intelligence Overview</h2>
            <p className="text-text-secondary">Real-time synthesis of global market opportunities.</p>
          </div>
        </div>

        {/* Stat Cards */}
        <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {statCards.map(({ label, value, icon: Icon, color }) => (
            <div key={label} className="bg-white p-6 rounded-xl shadow-sm relative overflow-hidden">
              <div className="flex justify-between items-start">
                <div>
                  <p className="text-xs font-bold uppercase tracking-widest text-text-secondary mb-1">{label}</p>
                  <h3 className={clsx("text-4xl font-extrabold tracking-tighter", color)}>
                    {value.toLocaleString()}
                  </h3>
                </div>
                <div className="bg-surface p-2 rounded-lg">
                  <Icon className={clsx("w-5 h-5", color)} />
                </div>
              </div>
              <div className="mt-4 flex items-center gap-1 text-success font-semibold text-sm">
                <TrendingUp className="w-4 h-4" />
                <span>Active</span>
              </div>
            </div>
          ))}
        </section>

        {/* Two Columns */}
        <section className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* Recent Deals */}
          <div className="bg-white rounded-xl shadow-sm">
            <div className="p-6 pb-3 flex justify-between items-center border-b border-border">
              <h4 className="text-lg font-bold text-primary flex items-center gap-2">
                <span className="w-1.5 h-6 bg-accent rounded-full" />
                Recent Deals
              </h4>
              <Link href="/deals" className="text-accent text-sm font-semibold hover:underline">
                View All
              </Link>
            </div>
            <div className="p-4 space-y-2">
              {recentDeals?.content.map((deal) => (
                <Link
                  key={deal.id}
                  href={`/deals/${deal.id}`}
                  className="flex items-center justify-between p-4 rounded-lg hover:bg-surface transition-colors group"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded bg-surface flex items-center justify-center font-bold text-primary text-xs">
                      {deal.title.split(" ").map((w) => w[0]).join("").slice(0, 2)}
                    </div>
                    <div>
                      <p className="font-bold text-primary group-hover:text-accent transition-colors text-sm">
                        {deal.title}
                      </p>
                      <div className="flex items-center gap-2 mt-0.5">
                        <span className="bg-surface text-text-secondary text-[10px] px-2 py-0.5 rounded font-bold uppercase">
                          {deal.industry}
                        </span>
                        <span className="text-xs text-text-secondary">
                          {deal.geography?.join(", ")}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className="font-bold text-primary text-sm">{deal.ticketSize}</p>
                    <p className="text-[10px] text-text-secondary">Ticket Size</p>
                  </div>
                </Link>
              ))}
              {(!recentDeals || recentDeals.content.length === 0) && (
                <p className="text-text-secondary text-sm text-center py-8">No deals yet</p>
              )}
            </div>
          </div>

          {/* Recent Matches */}
          <div className="bg-white rounded-xl shadow-sm">
            <div className="p-6 pb-3 flex justify-between items-center border-b border-border">
              <h4 className="text-lg font-bold text-primary flex items-center gap-2">
                <span className="w-1.5 h-6 bg-primary rounded-full" />
                Recent Matches
              </h4>
              <Link href="/matches" className="text-accent text-sm font-semibold hover:underline">
                View All
              </Link>
            </div>
            <div className="p-4 space-y-2">
              {recentMatches?.content.map((match) => (
                <div
                  key={match.id}
                  className="flex items-center justify-between p-4 rounded-lg hover:bg-surface transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center font-bold text-primary text-xs">
                      {match.profileName.split(" ").map((w) => w[0]).join("").slice(0, 2)}
                    </div>
                    <div>
                      <p className="font-bold text-primary text-sm">{match.profileName}</p>
                      <p className="text-xs text-text-secondary">
                        Match: <span className="font-semibold text-accent">{match.dealTitle}</span>
                      </p>
                    </div>
                  </div>
                  <div className="flex flex-col items-end">
                    <div className="flex items-center gap-2">
                      <div className="w-12 bg-surface h-1.5 rounded-full overflow-hidden">
                        <div
                          className={clsx("h-full rounded-full", {
                            "bg-success": match.relevanceScore >= 70,
                            "bg-warning": match.relevanceScore >= 40 && match.relevanceScore < 70,
                            "bg-error": match.relevanceScore < 40,
                          })}
                          style={{ width: `${match.relevanceScore}%` }}
                        />
                      </div>
                      <span className="text-xs font-extrabold text-primary">{match.relevanceScore}%</span>
                    </div>
                    <p className="text-[9px] text-text-secondary font-bold uppercase">Relevance</p>
                  </div>
                </div>
              ))}
              {(!recentMatches || recentMatches.content.length === 0) && (
                <p className="text-text-secondary text-sm text-center py-8">No matches yet</p>
              )}
            </div>
          </div>
        </section>

        {/* AI CTA */}
        <section className="bg-primary text-white p-8 rounded-xl relative overflow-hidden">
          <div className="relative z-10">
            <h4 className="text-xl font-bold mb-2">AI Generator: Strategic Synthesis</h4>
            <p className="text-blue-200 text-sm max-w-md">
              Use our neural engine to generate enterprise profiles from fragmented data sources.
            </p>
          </div>
          <div className="relative z-10 mt-6">
            <Link
              href="/ai-generator"
              className="bg-accent text-white font-bold px-6 py-2 rounded-lg text-sm hover:bg-sky-600 transition-colors inline-flex items-center gap-2"
            >
              Explore Analysis <ArrowRight className="w-4 h-4" />
            </Link>
          </div>
        </section>
      </div>
    </AppShell>
  );
}
