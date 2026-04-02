"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useState } from "react";
import { X } from "lucide-react";
import { Profile } from "@/lib/types";

const schema = z.object({
  name: z.string().min(1, "Name is required"),
  type: z.enum(["REAL", "SHADOW"]),
  servicesOffered: z.string().optional(),
  industryFocus: z.string().optional(),
  trackRecord: z.string().optional(),
  contactStatus: z.enum(["ACTIVE", "EXTERNAL", "NOT_ONBOARDED"]),
  summary: z.string().optional(),
});

type FormData = z.infer<typeof schema>;

interface Props {
  initial?: Profile;
  onSubmit: (data: FormData & { expertise: string[]; geographicReach: string[] }) => void;
  loading: boolean;
}

const industries = ["FinTech", "CleanTech", "Healthcare", "Logistics", "Commodities", "Cybersecurity", "Life Sciences", "Energy", "Blockchain", "Venture Capital", "Real Estate", "AI / Deep Learning"];
const regions = ["North America", "Europe", "EMEA", "APAC", "LATAM", "Middle East", "UK", "India", "Japan", "Dubai", "UAE", "Hong Kong", "Canada", "Global"];

export default function ProfileForm({ initial, onSubmit, loading }: Props) {
  const [expertise, setExpertise] = useState<string[]>(initial?.expertise ?? []);
  const [tagInput, setTagInput] = useState("");
  const [geoReach, setGeoReach] = useState<string[]>(initial?.geographicReach ?? []);

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      name: initial?.name ?? "",
      type: initial?.type ?? "REAL",
      servicesOffered: initial?.servicesOffered ?? "",
      industryFocus: initial?.industryFocus ?? "",
      trackRecord: initial?.trackRecord ?? "",
      contactStatus: initial?.contactStatus ?? "ACTIVE",
      summary: initial?.summary ?? "",
    },
  });

  function addTag(e: React.KeyboardEvent) {
    if (e.key === "Enter" && tagInput.trim()) {
      e.preventDefault();
      if (!expertise.includes(tagInput.trim())) {
        setExpertise([...expertise, tagInput.trim()]);
      }
      setTagInput("");
    }
  }

  function toggleGeo(region: string) {
    setGeoReach(prev => prev.includes(region) ? prev.filter(r => r !== region) : [...prev, region]);
  }

  return (
    <form onSubmit={handleSubmit((data) => onSubmit({ ...data, expertise, geographicReach: geoReach }))} className="space-y-8">
      <div className="grid grid-cols-12 gap-6">
        <div className="col-span-8 space-y-6">
          <div className="bg-white rounded-xl p-8 shadow-sm space-y-6">
            <h2 className="text-lg font-bold text-primary">Core Identity</h2>
            <div className="grid grid-cols-2 gap-6">
              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Name</label>
                <input {...register("name")} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
                {errors.name && <p className="text-error text-xs mt-1">{errors.name.message}</p>}
              </div>
              <div>
                <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Type</label>
                <select {...register("type")} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent">
                  <option value="REAL">Real Identity</option>
                  <option value="SHADOW">Shadow Persona</option>
                </select>
              </div>
            </div>

            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Expertise Tags</label>
              <div className="bg-surface rounded-lg p-3 flex flex-wrap gap-2 min-h-[48px]">
                {expertise.map(tag => (
                  <span key={tag} className="px-3 py-1 bg-primary text-white text-xs font-bold rounded-full flex items-center gap-1">
                    {tag}
                    <button type="button" onClick={() => setExpertise(expertise.filter(t => t !== tag))}><X className="w-3 h-3" /></button>
                  </span>
                ))}
                <input type="text" value={tagInput} onChange={e => setTagInput(e.target.value)} onKeyDown={addTag}
                  placeholder="Type and press Enter..." className="bg-transparent border-none text-xs focus:ring-0 flex-1 min-w-[100px] p-0" />
              </div>
            </div>

            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Industry Focus</label>
              <select {...register("industryFocus")} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent">
                <option value="">Select Industry</option>
                {industries.map(i => <option key={i} value={i}>{i}</option>)}
              </select>
            </div>

            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Services Offered</label>
              <textarea {...register("servicesOffered")} rows={3} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
            </div>
            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Track Record</label>
              <textarea {...register("trackRecord")} rows={3} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
            </div>
            <div>
              <label className="text-[11px] font-bold uppercase tracking-wider text-text-secondary block mb-2">Summary</label>
              <textarea {...register("summary")} rows={3} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
            </div>
          </div>
        </div>

        <div className="col-span-4 space-y-6">
          <div className="bg-primary text-white rounded-xl p-6">
            <h2 className="text-lg font-bold mb-4">Reach & Impact</h2>
            <label className="text-[10px] font-bold uppercase tracking-widest text-blue-200 block mb-3">Geographic Reach</label>
            <div className="grid grid-cols-2 gap-2">
              {regions.map(r => (
                <button key={r} type="button" onClick={() => toggleGeo(r)}
                  className={`px-3 py-2 rounded text-[11px] font-bold transition-all ${
                    geoReach.includes(r) ? "bg-accent text-white" : "bg-white/10 hover:bg-white/20"
                  }`}>{r}</button>
              ))}
            </div>

            <label className="text-[10px] font-bold uppercase tracking-widest text-blue-200 block mb-3 mt-6">Contact Status</label>
            <select {...register("contactStatus")} className="w-full bg-white/10 border-none rounded-lg p-3 text-sm text-white focus:ring-2 focus:ring-accent">
              <option value="ACTIVE" className="text-black">Active</option>
              <option value="EXTERNAL" className="text-black">External</option>
              <option value="NOT_ONBOARDED" className="text-black">Not Onboarded</option>
            </select>
          </div>
        </div>
      </div>

      <div className="flex justify-end gap-3">
        <button type="button" onClick={() => window.history.back()} className="px-6 py-2.5 text-sm font-bold text-text-primary hover:bg-surface rounded-lg transition-colors">Cancel</button>
        <button type="submit" disabled={loading}
          className="px-8 py-2.5 text-sm font-bold text-white bg-accent rounded-lg shadow-sm hover:bg-sky-600 transition-colors disabled:opacity-50">
          {loading ? "Saving..." : "Save Changes"}
        </button>
      </div>
    </form>
  );
}
