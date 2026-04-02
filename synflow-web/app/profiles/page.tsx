"use client";

import AppShell from "@/components/AppShell";
import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Profile, Page } from "@/lib/types";
import Link from "next/link";
import { useState } from "react";
import { Search, Plus, Filter } from "lucide-react";
import clsx from "clsx";

const statusColors: Record<string, string> = {
  ACTIVE: "bg-green-50 text-green-700 border-green-100",
  EXTERNAL: "bg-amber-50 text-amber-700 border-amber-100",
  NOT_ONBOARDED: "bg-slate-100 text-slate-600 border-slate-200",
};

const typeColors: Record<string, string> = {
  REAL: "bg-blue-50 text-blue-700",
  SHADOW: "bg-slate-100 text-slate-600",
};

export default function ProfilesPage() {
  const [search, setSearch] = useState("");
  const [industry, setIndustry] = useState("");
  const [type, setType] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery<Page<Profile>>({
    queryKey: ["profiles", search, industry, type, status, page],
    queryFn: () => {
      const params = new URLSearchParams();
      if (search) params.set("search", search);
      if (industry) params.set("industry", industry);
      if (type) params.set("type", type);
      if (status) params.set("status", status);
      params.set("page", String(page));
      params.set("size", "20");
      return api.get(`/api/profiles?${params}`).then((r) => r.data);
    },
  });

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex justify-between items-end">
          <div>
            <h2 className="text-3xl font-extrabold tracking-tight text-primary">Profiles Intelligence</h2>
            <p className="text-text-secondary mt-1">Manage and curate your network of sector experts and shadow profiles.</p>
          </div>
          <Link
            href="/profiles/new"
            className="flex items-center gap-2 bg-accent hover:bg-sky-600 text-white font-bold px-5 py-2.5 rounded-lg transition-colors shadow-sm"
          >
            <Plus className="w-4 h-4" /> Add Profile
          </Link>
        </div>

        {/* Filters */}
        <section className="bg-white p-4 rounded-xl flex flex-wrap gap-4 items-end shadow-sm">
          <div className="flex-1 min-w-[200px]">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
              <input
                type="text"
                placeholder="Search by name, industry..."
                value={search}
                onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                className="w-full bg-surface border-none rounded-lg text-sm py-2.5 pl-10 pr-4 focus:ring-2 focus:ring-accent"
              />
            </div>
          </div>
          <select value={industry} onChange={(e) => { setIndustry(e.target.value); setPage(0); }}
            className="bg-surface border-none rounded-lg text-sm py-2.5 px-3 focus:ring-2 focus:ring-accent">
            <option value="">All Industries</option>
            {["FinTech","CleanTech","Healthcare","Logistics","Commodities","Cybersecurity","Life Sciences","Energy","Blockchain","Venture Capital"].map(i => (
              <option key={i} value={i}>{i}</option>
            ))}
          </select>
          <select value={type} onChange={(e) => { setType(e.target.value); setPage(0); }}
            className="bg-surface border-none rounded-lg text-sm py-2.5 px-3 focus:ring-2 focus:ring-accent">
            <option value="">All Types</option>
            <option value="REAL">Real</option>
            <option value="SHADOW">Shadow</option>
          </select>
          <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}
            className="bg-surface border-none rounded-lg text-sm py-2.5 px-3 focus:ring-2 focus:ring-accent">
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option>
            <option value="EXTERNAL">External</option>
            <option value="NOT_ONBOARDED">Not Onboarded</option>
          </select>
          <button onClick={() => { setSearch(""); setIndustry(""); setType(""); setStatus(""); setPage(0); }}
            className="text-sm font-semibold text-accent hover:underline">Clear All</button>
        </section>

        {/* Table */}
        <div className="bg-white rounded-xl overflow-hidden shadow-sm">
          <table className="w-full text-left">
            <thead>
              <tr className="bg-surface border-b border-border">
                <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Profile</th>
                <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Expertise</th>
                <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Industry</th>
                <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Type</th>
                <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border/50">
              {data?.content.map((profile) => (
                <tr key={profile.id} className="hover:bg-surface/50 transition-colors cursor-pointer group"
                    onClick={() => window.location.href = `/profiles/${profile.id}`}>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs">
                        {profile.name.split(" ").map(w => w[0]).join("").slice(0,2)}
                      </div>
                      <div>
                        <div className="text-sm font-bold text-primary group-hover:text-accent transition-colors">{profile.name}</div>
                        <div className="text-xs text-text-secondary">{profile.uniqueCode}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex flex-wrap gap-1">
                      {profile.expertise?.slice(0, 3).map(tag => (
                        <span key={tag} className="px-2 py-0.5 rounded text-[10px] font-bold bg-accent/10 text-accent border border-accent/20 uppercase">
                          {tag}
                        </span>
                      ))}
                      {(profile.expertise?.length ?? 0) > 3 && (
                        <span className="text-[10px] text-text-secondary">+{profile.expertise.length - 3}</span>
                      )}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm font-medium">{profile.industryFocus}</div>
                    <div className="text-xs text-text-secondary">{profile.geographicReach?.join(", ")}</div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx("px-2.5 py-1 rounded-full text-[10px] font-bold uppercase", typeColors[profile.type])}>
                      {profile.type}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx("px-2.5 py-1 rounded-full text-[10px] font-bold uppercase border", statusColors[profile.contactStatus])}>
                      {profile.contactStatus.replace("_", " ")}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {isLoading && <div className="p-8 text-center"><div className="animate-spin w-6 h-6 border-2 border-accent border-t-transparent rounded-full mx-auto" /></div>}
          {data && data.content.length === 0 && <p className="p-8 text-center text-text-secondary text-sm">No profiles found</p>}

          {data && data.totalPages > 1 && (
            <div className="px-6 py-4 border-t border-border flex justify-between items-center">
              <span className="text-xs text-text-secondary">
                Showing {data.number * data.size + 1}-{Math.min((data.number + 1) * data.size, data.totalElements)} of {data.totalElements}
              </span>
              <div className="flex gap-1">
                <button disabled={page === 0} onClick={() => setPage(p => p - 1)}
                  className="px-3 py-1 rounded text-xs font-bold border border-border hover:bg-surface disabled:opacity-30">Prev</button>
                <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)}
                  className="px-3 py-1 rounded text-xs font-bold border border-border hover:bg-surface disabled:opacity-30">Next</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </AppShell>
  );
}
