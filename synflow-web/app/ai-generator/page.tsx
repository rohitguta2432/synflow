"use client";

import AppShell from "@/components/AppShell";
import { useMutation } from "@tanstack/react-query";
import api from "@/lib/api";
import { AIGenerateResponse } from "@/lib/types";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { Sparkles, Save, RefreshCw, X } from "lucide-react";

export default function AIGeneratorPage() {
  const router = useRouter();
  const [linkedinText, setLinkedinText] = useState("");
  const [websiteText, setWebsiteText] = useState("");
  const [freeText, setFreeText] = useState("");
  const [result, setResult] = useState<AIGenerateResponse | null>(null);
  const [editedResult, setEditedResult] = useState<AIGenerateResponse | null>(null);

  const generateMutation = useMutation({
    mutationFn: () => api.post("/api/ai/generate-profile", { linkedinText, websiteText, freeText }),
    onSuccess: (res) => { setResult(res.data); setEditedResult(res.data); },
  });

  const saveMutation = useMutation({
    mutationFn: () => api.post("/api/profiles", {
      name: editedResult!.name,
      type: "REAL",
      expertise: editedResult!.expertise,
      servicesOffered: editedResult!.servicesOffered,
      industryFocus: editedResult!.industryFocus,
      geographicReach: editedResult!.geographicReach,
      trackRecord: editedResult!.trackRecord,
      summary: editedResult!.summary,
      contactStatus: "ACTIVE",
      aiGenerated: true,
    }),
    onSuccess: (res) => router.push(`/profiles/${res.data.id}`),
  });

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight text-primary mb-2">AI Profile Generator</h2>
          <p className="text-text-secondary text-lg">Harness SynFlow Intelligence to curate deep enterprise profiles from fragmented data.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8">
          {/* Input */}
          <div className="lg:col-span-5 space-y-6">
            <div className="bg-white p-6 rounded-xl shadow-sm space-y-4">
              <h3 className="text-sm font-bold uppercase tracking-widest text-text-secondary">Source Intelligence</h3>
              <div>
                <label className="block text-xs font-bold text-text-secondary mb-2">LinkedIn Text</label>
                <textarea value={linkedinText} onChange={e => setLinkedinText(e.target.value)} rows={4}
                  placeholder="Paste LinkedIn about section..." className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
              </div>
              <div>
                <label className="block text-xs font-bold text-text-secondary mb-2">Website Text</label>
                <textarea value={websiteText} onChange={e => setWebsiteText(e.target.value)} rows={4}
                  placeholder="Paste website content..." className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
              </div>
              <div>
                <label className="block text-xs font-bold text-text-secondary mb-2">Free Text / Notes</label>
                <textarea value={freeText} onChange={e => setFreeText(e.target.value)} rows={4}
                  placeholder="Paste intro, bio, or meeting notes..." className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
              </div>
              <button onClick={() => generateMutation.mutate()} disabled={generateMutation.isPending || (!linkedinText && !websiteText && !freeText)}
                className="w-full bg-accent text-white py-4 rounded-lg font-extrabold flex items-center justify-center gap-3 shadow-md hover:bg-sky-600 transition-colors disabled:opacity-50">
                {generateMutation.isPending ? (
                  <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                ) : (
                  <><Sparkles className="w-5 h-5" /> Generate Profile</>
                )}
              </button>
            </div>
          </div>

          {/* Output */}
          <div className="lg:col-span-7">
            {editedResult ? (
              <div className="bg-white rounded-xl shadow-sm">
                <div className="p-6 border-b border-border flex justify-between items-center bg-surface/50 rounded-t-xl">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-primary rounded flex items-center justify-center text-white font-bold text-xl">
                      {editedResult.name.charAt(0)}
                    </div>
                    <div>
                      <input value={editedResult.name} onChange={e => setEditedResult({ ...editedResult, name: e.target.value })}
                        className="text-lg font-bold text-primary bg-transparent border-none focus:ring-0 p-0" />
                      <p className="text-xs text-text-secondary">AI Generated Profile</p>
                    </div>
                  </div>
                  <span className="px-3 py-1 bg-accent/10 text-accent text-[10px] font-bold rounded-full uppercase">Draft Preview</span>
                </div>

                <div className="p-8 space-y-6">
                  <div>
                    <h5 className="text-xs font-bold text-text-secondary uppercase tracking-widest mb-2">Summary</h5>
                    <textarea value={editedResult.summary} onChange={e => setEditedResult({ ...editedResult, summary: e.target.value })}
                      rows={3} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
                  </div>

                  <div className="grid grid-cols-2 gap-6">
                    <div>
                      <h5 className="text-xs font-bold text-text-secondary uppercase tracking-widest mb-2">Expertise</h5>
                      <div className="flex flex-wrap gap-2">
                        {editedResult.expertise.map((tag, i) => (
                          <span key={i} className="px-2 py-1 bg-primary text-white text-xs font-bold rounded-full flex items-center gap-1">
                            {tag}
                            <button onClick={() => setEditedResult({ ...editedResult, expertise: editedResult.expertise.filter((_, j) => j !== i) })}><X className="w-3 h-3" /></button>
                          </span>
                        ))}
                      </div>
                    </div>
                    <div>
                      <h5 className="text-xs font-bold text-text-secondary uppercase tracking-widest mb-2">Industry</h5>
                      <input value={editedResult.industryFocus} onChange={e => setEditedResult({ ...editedResult, industryFocus: e.target.value })}
                        className="w-full bg-surface border-none rounded-lg p-2 text-sm focus:ring-2 focus:ring-accent" />
                    </div>
                  </div>

                  <div>
                    <h5 className="text-xs font-bold text-text-secondary uppercase tracking-widest mb-2">Services</h5>
                    <textarea value={editedResult.servicesOffered} onChange={e => setEditedResult({ ...editedResult, servicesOffered: e.target.value })}
                      rows={2} className="w-full bg-surface border-none rounded-lg p-3 text-sm focus:ring-2 focus:ring-accent" />
                  </div>

                  <div>
                    <h5 className="text-xs font-bold text-text-secondary uppercase tracking-widest mb-2">Geographic Reach</h5>
                    <div className="flex flex-wrap gap-2">
                      {editedResult.geographicReach.map((r, i) => (
                        <span key={i} className="px-2 py-1 bg-surface text-xs font-bold rounded border border-border">{r}</span>
                      ))}
                    </div>
                  </div>
                </div>

                <div className="p-6 bg-surface rounded-b-xl flex justify-between items-center">
                  <button onClick={() => generateMutation.mutate()} className="text-text-secondary text-sm font-bold flex items-center gap-2 hover:text-primary">
                    <RefreshCw className="w-4 h-4" /> Regenerate
                  </button>
                  <button onClick={() => saveMutation.mutate()} disabled={saveMutation.isPending}
                    className="bg-primary text-white px-8 py-3 rounded-lg font-bold flex items-center gap-2 shadow-md hover:bg-primary/90 disabled:opacity-50">
                    <Save className="w-4 h-4" /> {saveMutation.isPending ? "Saving..." : "Save to Profiles"}
                  </button>
                </div>
              </div>
            ) : (
              <div className="bg-white rounded-xl shadow-sm p-12 text-center">
                <Sparkles className="w-12 h-12 text-border mx-auto mb-4" />
                <h3 className="text-lg font-bold text-primary mb-2">Ready to Generate</h3>
                <p className="text-text-secondary text-sm">Paste source data on the left and click Generate to create an AI-powered profile.</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </AppShell>
  );
}
