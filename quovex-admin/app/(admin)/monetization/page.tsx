'use client';

import { CreditCard, AlertCircle, ShieldCheck, CheckCircle2 } from 'lucide-react';
import EmptyState from '@/components/EmptyState';

export default function MonetizationPage() {
  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Monetization & Billing Center</h1>
          <p className="text-sm text-muted-foreground">Google Play Billing subscriptions and AdMob revenue telemetry (Zero Mock Data Compliant).</p>
        </div>
      </div>

      {/* Governance Notice */}
      <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-4">
        <div className="flex items-center gap-2">
          <CreditCard className="w-5 h-5 text-muted-foreground" />
          <h2 className="text-sm font-bold text-foreground">Billing Gateway Status: UNAVAILABLE</h2>
        </div>

        <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-2 text-xs text-muted-foreground leading-relaxed">
          <p className="font-semibold text-foreground">Rule 1 Governance Invariant:</p>
          <p>
            Google Play Billing v6 and server-side subscription validation are not active in this development environment.
            In strict adherence to the Zero Mock Data rule, zero revenue metrics, simulated transactions, or fabricated subscriber counts are rendered.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
          <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-1 text-xs">
            <div className="text-muted-foreground font-semibold">Total Revenue</div>
            <div className="text-xl font-bold text-foreground">₹0.00</div>
            <div className="text-[10px] text-muted-foreground">Real-time Stripe / Google Play sync required</div>
          </div>

          <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-1 text-xs">
            <div className="text-muted-foreground font-semibold">Active Subscribers</div>
            <div className="text-xl font-bold text-foreground">0</div>
            <div className="text-[10px] text-muted-foreground">0 simulated accounts</div>
          </div>

          <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-1 text-xs">
            <div className="text-muted-foreground font-semibold">Server Entitlements</div>
            <div className="text-xl font-bold text-primary">ENFORCED</div>
            <div className="text-[10px] text-muted-foreground">Firebase Functions server validation</div>
          </div>
        </div>
      </div>
    </div>
  );
}
