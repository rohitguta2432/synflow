-- Seed Users (passwords: admin123, user123 - BCrypt encoded)
INSERT INTO users (id, email, password_hash, full_name, role) VALUES
('a0000000-0000-0000-0000-000000000001', 'admin@synflow.com', '$2b$10$KueHswjJXKy0UsoJ3V3sfOT8JI/zhilu.Z3yI48Uc6JHiKhT1uKiy', 'Alex Sterling', 'ADMIN'),
('a0000000-0000-0000-0000-000000000002', 'user@synflow.com', '$2b$10$pxaOHQqSAN3t/fntJhocU.h8HxI7FyWIB1hZZlJIu.17CIjjznn96', 'Marcus Vane', 'INTERNAL_USER');

-- Seed Profiles (10 profiles, mix of Real and Shadow)
INSERT INTO profiles (id, unique_code, name, type, expertise, services_offered, industry_focus, geographic_reach, track_record, contact_status, summary, ai_generated, created_by) VALUES
('b0000000-0000-0000-0000-000000000001', 'Member_001', 'Julian Sterling', 'REAL',
 ARRAY['M&A Strategy', 'Cross-Border Assets', 'Tech Consolidation', 'Venture Scouting'],
 'Deal origination, due diligence, syndicate structuring', 'FinTech',
 ARRAY['EMEA', 'North America'],
 '$2.4B combined track record across 45+ transactions', 'ACTIVE',
 'Strategic M&A advisor specializing in cross-border FinTech transactions with deep PE expertise.', false,
 'a0000000-0000-0000-0000-000000000001'),

('b0000000-0000-0000-0000-000000000002', 'Member_002', 'Elena Rodriguez', 'REAL',
 ARRAY['ESG Investing', 'Impact Assessment', 'Green Bonds', 'Carbon Credits'],
 'Sustainability advisory, ESG compliance, green finance structuring', 'CleanTech',
 ARRAY['EMEA', 'LATAM'],
 'Led $800M in green bond issuances. Pioneer in carbon credit marketplace design.', 'ACTIVE',
 'Leading ESG and sustainability advisor driving green finance innovation across emerging markets.', false,
 'a0000000-0000-0000-0000-000000000001'),

('b0000000-0000-0000-0000-000000000003', 'External_001', 'Shadow_Node_712', 'SHADOW',
 ARRAY['Kubernetes', 'GoLang', 'Cloud Security', 'Zero Trust'],
 'Infrastructure modernization, security architecture review', 'Cybersecurity',
 ARRAY['Europe'],
 'Not specified', 'NOT_ONBOARDED',
 'Shadow profile for VP Engineering role in European cybersecurity sector.', false,
 'a0000000-0000-0000-0000-000000000001'),

('b0000000-0000-0000-0000-000000000004', 'Member_003', 'Alice Martineau', 'REAL',
 ARRAY['Genomics', 'CRISPR', 'Drug Discovery', 'Clinical Trials'],
 'Biotech advisory, regulatory pathway strategy, IP portfolio management', 'Life Sciences',
 ARRAY['Canada', 'Global'],
 'CSO with 3 successful FDA approvals and $400M in biotech fundraising.', 'EXTERNAL',
 'Chief Science Officer bridging genomics research and commercial drug development.', false,
 'a0000000-0000-0000-0000-000000000001'),

('b0000000-0000-0000-0000-000000000005', 'Member_004', 'Satoshi Kawamoto', 'REAL',
 ARRAY['Supply Chain', 'Automation', 'IoT Logistics', 'Fleet Management'],
 'Logistics optimization, warehouse automation, last-mile delivery solutions', 'Logistics',
 ARRAY['Japan', 'APAC'],
 'Transformed Nippon Air logistics with 40% efficiency gain through IoT automation.', 'ACTIVE',
 'Head of Logistics driving next-gen supply chain automation across APAC.', false,
 'a0000000-0000-0000-0000-000000000001'),

('b0000000-0000-0000-0000-000000000006', 'Member_005', 'Marcus Chen', 'REAL',
 ARRAY['OTC Trading', 'Gold Trading', 'Commodity Markets', 'Derivatives'],
 'OTC brokerage, precious metals advisory, commodity hedging strategies', 'Commodities',
 ARRAY['Dubai', 'UAE', 'Hong Kong'],
 '$1.2B annual OTC volume in precious metals. Licensed in DMCC and HKEX.', 'ACTIVE',
 'Senior OTC trader and commodity markets specialist operating across Middle East and Asia.', false,
 'a0000000-0000-0000-0000-000000000002'),

('b0000000-0000-0000-0000-000000000007', 'External_002', 'Shadow_Advisory_88', 'SHADOW',
 ARRAY['Fundraising', 'Seed Capital', 'Angel Networks', 'Pitch Coaching'],
 'Early stage fundraising advisory, investor introductions', 'Venture Capital',
 ARRAY['North America', 'Europe'],
 'Not specified', 'NOT_ONBOARDED',
 'Shadow profile for early-stage fundraising advisory network.', false,
 'a0000000-0000-0000-0000-000000000002'),

('b0000000-0000-0000-0000-000000000008', 'Member_006', 'Sarah Jenkins', 'REAL',
 ARRAY['Healthcare AI', 'Medical Devices', 'Telemedicine', 'FDA Compliance'],
 'HealthTech advisory, digital health strategy, regulatory navigation', 'Healthcare',
 ARRAY['North America', 'UK'],
 'Advised on 12 digital health exits totaling $600M. Ex-McKinsey healthcare practice.', 'ACTIVE',
 'Strategic HealthTech advisor bridging clinical innovation and investor ecosystems.', false,
 'a0000000-0000-0000-0000-000000000001'),

('b0000000-0000-0000-0000-000000000009', 'Member_007', 'Raj Patel', 'REAL',
 ARRAY['Renewable Energy', 'Solar PV', 'Grid Infrastructure', 'Project Finance'],
 'Renewable energy project development, grid modernization advisory', 'Energy',
 ARRAY['India', 'APAC', 'Middle East'],
 'Developed 2.5GW of solar capacity across South Asia. Former World Bank energy consultant.', 'ACTIVE',
 'Renewable energy project developer with deep expertise in emerging market grid infrastructure.', false,
 'a0000000-0000-0000-0000-000000000002'),

('b0000000-0000-0000-0000-000000000010', 'External_003', 'Shadow_Blockchain_99', 'SHADOW',
 ARRAY['DeFi', 'Smart Contracts', 'Tokenization', 'Web3'],
 'Blockchain advisory, DeFi protocol design, token economics', 'Blockchain',
 ARRAY['Global'],
 'Not specified', 'NOT_ONBOARDED',
 'Shadow profile for blockchain and DeFi advisory capabilities.', false,
 'a0000000-0000-0000-0000-000000000002');

-- Seed Deals (5 deals)
INSERT INTO deals (id, title, industry, deal_type, ticket_size, geography, requirements, status, created_by) VALUES
('c0000000-0000-0000-0000-000000000001', 'SustainCloud Series C', 'CleanTech', 'INVESTMENT', '$45M',
 ARRAY['EMEA'],
 'Seeking growth equity for European SaaS platform focused on carbon accounting. Requires ESG expertise and European market access.',
 'ACTIVE', 'a0000000-0000-0000-0000-000000000001'),

('c0000000-0000-0000-0000-000000000002', 'Nova Ventures Acquisition', 'FinTech', 'ADVISORY', '$120M',
 ARRAY['North America'],
 'Cross-border M&A advisory for payment processing company. Need M&A strategy expertise and regulatory knowledge.',
 'ACTIVE', 'a0000000-0000-0000-0000-000000000001'),

('c0000000-0000-0000-0000-000000000003', 'BioTrend Expansion', 'Healthcare', 'PARTNERSHIP', '$12.5M',
 ARRAY['APAC', 'North America'],
 'Partnership opportunity in telemedicine and healthcare AI. Looking for medical device expertise and FDA compliance knowledge.',
 'ACTIVE', 'a0000000-0000-0000-0000-000000000002'),

('c0000000-0000-0000-0000-000000000004', 'Gold OTC Market Entry', 'Commodities', 'BROKERAGE', '$50M-100M',
 ARRAY['Dubai', 'UAE', 'Hong Kong'],
 'OTC gold trading desk setup. Requires commodity markets expertise, DMCC licensing, and Middle East network.',
 'ACTIVE', 'a0000000-0000-0000-0000-000000000001'),

('c0000000-0000-0000-0000-000000000005', 'GreenGrid Infrastructure', 'Energy', 'INVESTMENT', '$200M',
 ARRAY['India', 'Middle East', 'APAC'],
 'Large-scale solar PV and grid infrastructure project. Need renewable energy expertise and project finance capabilities.',
 'ACTIVE', 'a0000000-0000-0000-0000-000000000002');

-- Seed Profile Connections
INSERT INTO profile_connections (profile_id, connected_profile_id, connection_type) VALUES
('b0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 'business partner'),
('b0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000006', 'referral'),
('b0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000009', 'advisor'),
('b0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000009', 'business partner'),
('b0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000008', 'referral');

-- Seed Matches (pre-computed for demo)
INSERT INTO matches (deal_id, profile_id, relevance_score, match_reason) VALUES
('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000002', 80.00, 'Matched on: CleanTech (partial industry match via expertise), ESG Investing (expertise), Green Bonds (expertise), EMEA (geography)'),
('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000009', 30.00, 'Matched on: Renewable Energy (partial industry match via expertise)'),
('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000001', 70.00, 'Matched on: FinTech (industry match), M&A Strategy (expertise), North America (geography)'),
('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000008', 80.00, 'Matched on: Healthcare (industry match), Healthcare AI (expertise), Telemedicine (expertise), North America (geography)'),
('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000004', 40.00, 'Matched on: Healthcare (partial industry match via expertise)'),
('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000006', 100.00, 'Matched on: Commodities (industry match), OTC Trading (expertise), Gold Trading (expertise), Dubai (geography), UAE (geography)'),
('c0000000-0000-0000-0000-000000000005', 'b0000000-0000-0000-0000-000000000009', 90.00, 'Matched on: Energy (industry match), Renewable Energy (expertise), Solar PV (expertise), India (geography), Middle East (geography)');
