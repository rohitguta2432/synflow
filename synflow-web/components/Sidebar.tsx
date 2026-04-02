"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useState } from "react";
import {
  LayoutDashboard, Users, Handshake, Sparkles, Brain, Settings,
  HelpCircle, Plus, ChevronLeft, ChevronRight,
} from "lucide-react";
import clsx from "clsx";

const navItems = [
  { href: "/dashboard", icon: LayoutDashboard, label: "Dashboard" },
  { href: "/profiles", icon: Users, label: "Profiles" },
  { href: "/deals", icon: Handshake, label: "Deals" },
  { href: "/matches", icon: Sparkles, label: "Matches" },
  { href: "/ai-generator", icon: Brain, label: "AI Generator" },
  { href: "/settings", icon: Settings, label: "Settings" },
];

export default function Sidebar() {
  const pathname = usePathname();
  const [collapsed, setCollapsed] = useState(false);

  return (
    <aside
      className={clsx(
        "h-screen fixed left-0 top-0 bg-primary flex flex-col py-6 z-50 transition-all duration-300",
        collapsed ? "w-16" : "w-64"
      )}
    >
      <div className={clsx("px-6 mb-10 flex items-center gap-3", collapsed && "px-3 justify-center")}>
        <div className="w-8 h-8 bg-accent rounded flex items-center justify-center flex-shrink-0">
          <Sparkles className="w-5 h-5 text-white" />
        </div>
        {!collapsed && (
          <div>
            <h1 className="text-xl font-bold tracking-tighter text-white">SynFlow</h1>
            <p className="text-[10px] uppercase tracking-[0.2em] text-blue-300 font-semibold">
              Enterprise Intelligence
            </p>
          </div>
        )}
      </div>

      <nav className="flex-1 space-y-1">
        {navItems.map(({ href, icon: Icon, label }) => {
          const active = pathname.startsWith(href);
          return (
            <Link
              key={href}
              href={href}
              className={clsx(
                "flex items-center gap-3 px-4 py-3 transition-all duration-200",
                collapsed && "justify-center px-0",
                active
                  ? "border-l-4 border-accent bg-white/10 text-white font-semibold"
                  : "text-slate-400 hover:text-white hover:bg-white/5"
              )}
            >
              <Icon className="w-5 h-5 flex-shrink-0" />
              {!collapsed && <span className="text-sm">{label}</span>}
            </Link>
          );
        })}
      </nav>

      <div className="mt-auto px-4 space-y-3">
        {!collapsed && (
          <Link
            href="/profiles/new"
            className="w-full bg-accent text-white font-bold py-2.5 rounded-lg text-sm flex items-center justify-center gap-2 hover:bg-sky-600 transition-colors"
          >
            <Plus className="w-4 h-4" /> New Analysis
          </Link>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="w-full flex items-center justify-center gap-2 py-2 text-slate-400 hover:text-white transition-colors"
        >
          {collapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          {!collapsed && <span className="text-xs">Collapse</span>}
        </button>
        <Link
          href="#"
          className={clsx(
            "flex items-center gap-3 py-3 text-slate-400 hover:text-white transition-colors",
            collapsed && "justify-center"
          )}
        >
          <HelpCircle className="w-5 h-5" />
          {!collapsed && <span className="text-sm">Support</span>}
        </Link>
      </div>
    </aside>
  );
}
