"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useState } from "react";
import { Deal } from "@/lib/types";

const schema = z.object({
  title: z.string().min(1, "Title is required"),
  industry: z.string().min(1, "Industry is required"),
  dealType: z.enum(["INVESTMENT", "PARTNERSHIP", "ADVISORY", "BROKERAGE", "OTHER"]),
  ticketSize: z.string().optional(),
  requirements: z.string().optional(),
  status: z.enum(["ACTIVE", "CLOSED", "DRAFT"]),
});

type FormData = z.infer<typeof schema>;

const regions = ["North America", "Europe", "EMEA", "APAC", "LATAM", "Middle East", "UK", "India", "Japan", "Dubai", "UAE", "Hong Kong", "Canada", "Global"];

export default function DealForm({ initial, onSubmit, loading }: { initial?: Deal; onSubmit: (data: FormData & { geography: string[] }) => void; loading: boolean }) {
  const [geography, setGeography] = useState<string[]>(initial?.geography ?? []);

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      title: initial?.title ?? "", industry: initial?.industry ?? "", dealType: (initial?.dealType as FormData["dealType"]) ?? "INVESTMENT",
      ticketSize: initial?.ticketSize ?? "", requirements: initial?.requirements ?? "", status: (initial?.status as FormData["status"]) ?? "ACTIVE",
    },
  });

  return (
    <form onSubmit={handleSubmit((data) => onSubmit({ ...data, geography }))} className="space-y-6">
      <div className="bg-white rounded-xl p-8 shadow-sm space-y-6 max-w-3xl">
        <div className="grid grid-cols-2 gap-6">
          <div className="col-span-2">
            <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Title</label>
            <input {...register("title")} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
            {errors.title && <p className="text-error text-xs mt-1">{errors.title.message}</p>}
          </div>
          <div>
            <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Industry</label>
            <select {...register("industry")} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent">
              <option value="">Select</option>
              {["FinTech","CleanTech","Healthcare","Commodities","Energy","Logistics","Cybersecurity","Blockchain","Real Estate","AI / Deep Learning"].map(i => <option key={i} value={i}>{i}</option>)}
            </select>
            {errors.industry && <p className="text-error text-xs mt-1">{errors.industry.message}</p>}
          </div>
          <div>
            <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Deal Type</label>
            <select {...register("dealType")} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent">
              {["INVESTMENT","PARTNERSHIP","ADVISORY","BROKERAGE","OTHER"].map(t => <option key={t} value={t}>{t}</option>)}
            </select>
          </div>
          <div>
            <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Ticket Size</label>
            <input {...register("ticketSize")} placeholder="e.g., $1M-5M" className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
          </div>
          <div>
            <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Status</label>
            <select {...register("status")} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent">
              <option value="ACTIVE">Active</option><option value="DRAFT">Draft</option><option value="CLOSED">Closed</option>
            </select>
          </div>
        </div>

        <div>
          <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Geography</label>
          <div className="flex flex-wrap gap-2">
            {regions.map(r => (
              <button key={r} type="button" onClick={() => setGeography(prev => prev.includes(r) ? prev.filter(g => g !== r) : [...prev, r])}
                className={`px-3 py-1.5 rounded text-xs font-bold transition-all ${geography.includes(r) ? "bg-accent text-white" : "bg-surface text-text-secondary hover:bg-border"}`}>{r}</button>
            ))}
          </div>
        </div>

        <div>
          <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Requirements</label>
          <textarea {...register("requirements")} rows={4} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
        </div>
      </div>

      <div className="flex gap-3">
        <button type="button" onClick={() => window.history.back()} className="px-6 py-2.5 text-sm font-bold text-text-primary hover:bg-surface rounded-lg">Cancel</button>
        <button type="submit" disabled={loading} className="px-8 py-2.5 text-sm font-bold text-white bg-accent rounded-lg hover:bg-sky-600 disabled:opacity-50">{loading ? "Saving..." : "Save Deal"}</button>
      </div>
    </form>
  );
}
