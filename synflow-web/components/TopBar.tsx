"use client";

import { Bell, MessageSquare, Search, LogOut } from "lucide-react";
import { getUser, logout } from "@/lib/auth";
import { useEffect, useState } from "react";
import { User } from "@/lib/types";

export default function TopBar() {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    setUser(getUser());
  }, []);

  return (
    <header className="flex justify-between items-center px-8 h-16 bg-white border-b border-border">
      <div className="relative w-full max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-text-secondary w-4 h-4" />
        <input
          type="text"
          placeholder="Search enterprise intelligence..."
          className="w-full bg-surface border-none rounded-full py-2 pl-10 pr-4 text-sm focus:ring-2 focus:ring-accent focus:outline-none"
        />
      </div>

      <div className="flex items-center gap-5">
        <button className="text-text-secondary hover:text-primary transition-colors relative">
          <Bell className="w-5 h-5" />
          <span className="absolute -top-0.5 -right-0.5 w-2 h-2 bg-error rounded-full" />
        </button>
        <button className="text-text-secondary hover:text-primary transition-colors">
          <MessageSquare className="w-5 h-5" />
        </button>

        <div className="h-6 w-px bg-border" />

        {user && (
          <div className="flex items-center gap-3">
            <div className="text-right">
              <p className="text-sm font-semibold text-primary">{user.fullName}</p>
              <p className="text-[10px] text-text-secondary">{user.role === "ADMIN" ? "Admin" : "Analyst"}</p>
            </div>
            <div className="w-9 h-9 rounded-full bg-primary flex items-center justify-center text-white text-xs font-bold">
              {user.fullName.split(" ").map((n) => n[0]).join("")}
            </div>
          </div>
        )}

        <button
          onClick={logout}
          className="text-text-secondary hover:text-error transition-colors"
          title="Sign out"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
}
