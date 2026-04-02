"use client";

import AppShell from "@/components/AppShell";
import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Deal, Page } from "@/lib/types";
import Link from "next/link";
import { useState } from "react";
import { Search, Plus, MapPin } from "lucide-react";
import clsx from "clsx";

const statusColors: Record<string, string> = {
  ACTIVE: "bg-green-50 text-green-700", CLOSED: "bg-slate-100 text-slate-600", DRAFT: "bg-amber-50 text-amber-700",
};

const typeColors: Record<string, string> = {
  INVESTMENT: "bg-blue-50 text-blue-700", PARTNERSHIP: "bg-purple-50 text-purple-700",
  ADVISORY: "bg-teal-50 text-teal-700", BROKERAGE: "bg-amber-50 text-amber-700", OTHER: "bg-slate-100 text-slate-600",
};

export default function DealsPage() {
  const [search, setSearch] = useState("");
  const [industry, setIndustry] = useState("");
  const [dealType, setDealType] = useState("");
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);

  const { data, isLoading } = useQuery<Page<Deal>>({
    queryKey: ["deals", search, industry, dealType, status, page],
    queryFn: () => {
      const params = new URLSearchParams();
      if (search) params.set("search", search);
      if (industry) params.set("industry", industry);
      if (dealType) params.set("dealType", dealType);
      if (status) params.set("status", status);
      params.set("page", String(page));
      params.set("size", "20");
      return api.get(`/api/deals?${params}`).then((r) => r.data);
    },
  });

  return (
    <AppShell>
      <div className="space-y-6">
        <div className="flex justify-between items-end">
          <div>
            <h2 className="text-3xl font-extrabold tracking-tight text-primary">Deals Pipeline</h2>
            <p className="text-text-secondary mt-1">Managing enterprise transactions across global markets.</p>
          </div>
          <Link href="/deals/new" className="flex items-center gap-2 bg-accent hover:bg-sky-600 text-white font-bold px-5 py-2.5 rounded-lg transition-colors shadow-sm">
            <Plus className="w-4 h-4" /> Create Deal
          </Link>
        </div>

        <section className="bg-white p-4 rounded-xl flex flex-wrap gap-4 items-end shadow-sm">
          <div className="flex-1 min-w-[200px]">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-text-secondary" />
              <input type="text" placeholder="Search deals..." value={search} onChange={(e) => { setSearch(e.target.value); setPage(0); }}
                className="w-full bg-surface border-none rounded-lg text-sm py-2.5 pl-10 pr-4 focus:ring-2 focus:ring-accent" />
            </div>
          </div>
          <select value={industry} onChange={(e) => { setIndustry(e.target.value); setPage(0); }}
            className="bg-surface border-none rounded-lg text-sm py-2.5 px-3 focus:ring-2 focus:ring-accent">
            <option value="">All Industries</option>
            {["FinTech","CleanTech","Healthcare","Commodities","Energy","Logistics"].map(i => <option key={i} value={i}>{i}</option>)}
          </select>
          <select value={dealType} onChange={(e) => { setDealType(e.target.value); setPage(0); }}
            className="bg-surface border-none rounded-lg text-sm py-2.5 px-3 focus:ring-2 focus:ring-accent">
            <option value="">All Types</option>
            {["INVESTMENT","PARTNERSHIP","ADVISORY","BROKERAGE","OTHER"].map(t => <option key={t} value={t}>{t}</option>)}
          </select>
          <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}
            className="bg-surface border-none rounded-lg text-sm py-2.5 px-3 focus:ring-2 focus:ring-accent">
            <option value="">All Status</option>
            <option value="ACTIVE">Active</option><option value="CLOSED">Closed</option><option value="DRAFT">Draft</option>
          </select>
        </section>

        <div className="bg-white rounded-xl overflow-hidden shadow-sm">
          <table className="w-full text-left">
            <thead><tr className="bg-surface border-b border-border">
              <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Deal Title</th>
              <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Industry</th>
              <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Type</th>
              <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest text-right">Ticket</th>
              <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Geography</th>
              <th className="px-6 py-4 text-[11px] font-bold text-text-secondary uppercase tracking-widest">Status</th>
            </tr></thead>
            <tbody className="divide-y divide-border/50">
              {data?.content.map(deal => (
                <tr key={deal.id} className="hover:bg-surface/50 transition-colors cursor-pointer group"
                    onClick={() => window.location.href = `/deals/${deal.id}`}>
                  <td className="px-6 py-4">
                    <span className="font-bold text-primary group-hover:text-accent transition-colors text-sm">{deal.title}</span>
                  </td>
                  <td className="px-6 py-4 text-sm">{deal.industry}</td>
                  <td className="px-6 py-4">
                    <span className={clsx("text-xs font-bold px-2 py-1 rounded", typeColors[deal.dealType])}>{deal.dealType}</span>
                  </td>
                  <td className="px-6 py-4 text-right font-bold text-primary text-sm tabular-nums">{deal.ticketSize}</td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-1 text-sm text-text-secondary">
                      <MapPin className="w-3 h-3" /> {deal.geography?.join(", ")}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span className={clsx("px-2.5 py-1 rounded-full text-[10px] font-bold uppercase", statusColors[deal.status])}>{deal.status}</span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {isLoading && <div className="p-8 text-center"><div className="animate-spin w-6 h-6 border-2 border-accent border-t-transparent rounded-full mx-auto" /></div>}
          {data && data.content.length === 0 && <p className="p-8 text-center text-text-secondary text-sm">No deals found</p>}

          {data && data.totalPages > 1 && (
            <div className="px-6 py-4 border-t border-border flex justify-between items-center">
              <span className="text-xs text-text-secondary">Page {data.number + 1} of {data.totalPages}</span>
              <div className="flex gap-1">
                <button disabled={page === 0} onClick={() => setPage(p => p - 1)} className="px-3 py-1 rounded text-xs font-bold border border-border hover:bg-surface disabled:opacity-30">Prev</button>
                <button disabled={page >= data.totalPages - 1} onClick={() => setPage(p => p + 1)} className="px-3 py-1 rounded text-xs font-bold border border-border hover:bg-surface disabled:opacity-30">Next</button>
              </div>
            </div>
          )}
        </div>
      </div>
    </AppShell>
  );
}
