export interface User {
  id: string;
  email: string;
  fullName: string;
  role: "ADMIN" | "INTERNAL_USER";
  createdAt: string;
}

export interface Profile {
  id: string;
  uniqueCode: string;
  name: string;
  type: "REAL" | "SHADOW";
  expertise: string[];
  servicesOffered: string;
  industryFocus: string;
  geographicReach: string[];
  trackRecord: string;
  contactStatus: "ACTIVE" | "EXTERNAL" | "NOT_ONBOARDED";
  summary: string;
  aiGenerated: boolean;
  createdByName: string;
  connections: Connection[];
  matches: Match[];
  createdAt: string;
  updatedAt: string;
}

export interface Connection {
  id: string;
  connectedProfileId: string;
  connectedProfileName: string;
  connectedProfileType: string;
  connectedProfileIndustry: string;
  connectionType: string;
}

export interface Deal {
  id: string;
  title: string;
  industry: string;
  dealType: string;
  ticketSize: string;
  geography: string[];
  requirements: string;
  status: "ACTIVE" | "CLOSED" | "DRAFT";
  createdByName: string;
  matches: Match[];
  createdAt: string;
  updatedAt: string;
}

export interface Match {
  id: string;
  dealId: string;
  dealTitle: string;
  dealIndustry: string;
  profileId: string;
  profileName: string;
  profileExpertise: string[];
  relevanceScore: number;
  matchReason: string;
  matchedAt: string;
}

export interface Stats {
  totalProfiles: number;
  totalDeals: number;
  totalMatches: number;
}

export interface AIGenerateResponse {
  name: string;
  expertise: string[];
  servicesOffered: string;
  industryFocus: string;
  geographicReach: string[];
  trackRecord: string;
  summary: string;
  suggestedTags: string[];
}

export interface GraphData {
  nodes: { id: string; name: string; type: string; industry: string }[];
  edges: { source: string; target: string; connectionType: string }[];
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
