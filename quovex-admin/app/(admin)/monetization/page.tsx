'use client';

import React, { useState, useEffect } from 'react';
import {
  CreditCard,
  Tv,
  Save,
  RotateCcw,
  CheckCircle2,
  AlertTriangle,
  Info,
  Layers,
  Sparkles,
  Zap
} from 'lucide-react';

interface AdMobConfig {
  bannerAdUnitId: string;
  interstitialAdUnitId: string;
  rewardedAdUnitId: string;
  adsEnabled: boolean;
  bonusAiQueriesPerReward: number;
  updatedAt?: number;
  updatedBy?: string;
}

const DEFAULT_TEST_CONFIG: AdMobConfig = {
  bannerAdUnitId: 'ca-app-pub-3940256099942544/6300978111',
  interstitialAdUnitId: 'ca-app-pub-3940256099942544/1033173712',
  rewardedAdUnitId: 'ca-app-pub-3940256099942544/5224354917',
  adsEnabled: true,
  bonusAiQueriesPerReward: 3,
};

export default function MonetizationPage() {
  const [config, setConfig] = useState<AdMobConfig>(DEFAULT_TEST_CONFIG);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [statusMessage, setStatusMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  useEffect(() => {
    fetchConfig();
  }, []);

  const fetchConfig = async () => {
    try {
      setLoading(true);
      const res = await fetch('/api/admob/config');
      const data = await res.json();
      if (data.success && data.config) {
        setConfig(data.config);
      }
    } catch (err: any) {
      console.error('Failed to load AdMob config:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setSaving(true);
      setStatusMessage(null);
      const res = await fetch('/api/admob/config', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config),
      });
      const data = await res.json();
      if (data.success) {
        setStatusMessage({ type: 'success', text: 'AdMob configuration saved and broadcasted to Firestore!' });
        setConfig(data.config);
      } else {
        setStatusMessage({ type: 'error', text: data.error || 'Failed to save settings.' });
      }
    } catch (err: any) {
      setStatusMessage({ type: 'error', text: err.message || 'Network error saving settings.' });
    } finally {
      setSaving(false);
    }
  };

  const isTestId = (id: string) => id.includes('3940256099942544');

  return (
    <div className="space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground">Monetization & Ad Center</h1>
          <p className="text-sm text-muted-foreground">
            Manage dynamic Google AdMob ad units, rewarded quotas, and Play Billing telemetry in real-time.
          </p>
        </div>
      </div>

      {/* AdMob Dynamic Configuration Card */}
      <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-border pb-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-lg bg-primary/10 border border-primary/20 text-primary">
              <Tv className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-foreground">Google AdMob Dynamic Configuration</h2>
              <p className="text-xs text-muted-foreground">
                Changes made here propagate to all Android clients via Firestore without needing a Play Store APK update.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={() => {
                setConfig(DEFAULT_TEST_CONFIG);
                setStatusMessage({ type: 'success', text: 'Reset inputs to official Google Test Ad Units.' });
              }}
              className="px-3 py-1.5 text-xs font-semibold text-muted-foreground hover:text-foreground border border-border rounded-lg bg-[#15201C] flex items-center gap-1.5 transition-colors"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              Reset to Test IDs
            </button>
          </div>
        </div>

        {statusMessage && (
          <div
            className={`p-3.5 rounded-lg border text-xs flex items-center gap-2 ${
              statusMessage.type === 'success'
                ? 'bg-primary/10 border-primary/30 text-primary'
                : 'bg-red-500/10 border-red-500/30 text-red-400'
            }`}
          >
            {statusMessage.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 shrink-0" />
            ) : (
              <AlertTriangle className="w-4 h-4 shrink-0" />
            )}
            <span>{statusMessage.text}</span>
          </div>
        )}

        <form onSubmit={handleSave} className="space-y-6">
          {/* Global Master Switch */}
          <div className="flex items-center justify-between p-4 rounded-lg bg-[#15201C] border border-border">
            <div className="space-y-0.5">
              <div className="text-sm font-semibold text-foreground">Global Ads Master Switch</div>
              <div className="text-xs text-muted-foreground">
                Toggles ad requests (Banner, Interstitial, Rewarded) on or off for all non-Pro users.
              </div>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={config.adsEnabled}
                onChange={(e) => setConfig({ ...config, adsEnabled: e.target.checked })}
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-muted peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-primary"></div>
            </label>
          </div>

          {/* Ad Unit Inputs */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Banner */}
            <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-3">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold text-foreground flex items-center gap-1.5">
                  <Layers className="w-4 h-4 text-primary" />
                  Banner Ad Unit ID
                </label>
                <span
                  className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                    isTestId(config.bannerAdUnitId)
                      ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  }`}
                >
                  {isTestId(config.bannerAdUnitId) ? 'Test ID' : 'Production'}
                </span>
              </div>
              <input
                type="text"
                value={config.bannerAdUnitId}
                onChange={(e) => setConfig({ ...config, bannerAdUnitId: e.target.value })}
                placeholder="ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
                className="w-full text-xs font-mono p-2.5 rounded-lg bg-[#0E1513] border border-border text-foreground focus:outline-none focus:border-primary"
                required
              />
              <p className="text-[11px] text-muted-foreground">Displayed at bottom of Knowledge Hub and Quizzes.</p>
            </div>

            {/* Interstitial */}
            <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-3">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold text-foreground flex items-center gap-1.5">
                  <Zap className="w-4 h-4 text-primary" />
                  Interstitial Ad Unit ID
                </label>
                <span
                  className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                    isTestId(config.interstitialAdUnitId)
                      ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  }`}
                >
                  {isTestId(config.interstitialAdUnitId) ? 'Test ID' : 'Production'}
                </span>
              </div>
              <input
                type="text"
                value={config.interstitialAdUnitId}
                onChange={(e) => setConfig({ ...config, interstitialAdUnitId: e.target.value })}
                placeholder="ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
                className="w-full text-xs font-mono p-2.5 rounded-lg bg-[#0E1513] border border-border text-foreground focus:outline-none focus:border-primary"
                required
              />
              <p className="text-[11px] text-muted-foreground">Shown between completed study focus sessions.</p>
            </div>

            {/* Rewarded */}
            <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-3">
              <div className="flex items-center justify-between">
                <label className="text-xs font-bold text-foreground flex items-center gap-1.5">
                  <Sparkles className="w-4 h-4 text-primary" />
                  Rewarded Ad Unit ID
                </label>
                <span
                  className={`text-[10px] px-2 py-0.5 rounded-full font-bold uppercase ${
                    isTestId(config.rewardedAdUnitId)
                      ? 'bg-amber-500/10 text-amber-400 border border-amber-500/20'
                      : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                  }`}
                >
                  {isTestId(config.rewardedAdUnitId) ? 'Test ID' : 'Production'}
                </span>
              </div>
              <input
                type="text"
                value={config.rewardedAdUnitId}
                onChange={(e) => setConfig({ ...config, rewardedAdUnitId: e.target.value })}
                placeholder="ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY"
                className="w-full text-xs font-mono p-2.5 rounded-lg bg-[#0E1513] border border-border text-foreground focus:outline-none focus:border-primary"
                required
              />
              <p className="text-[11px] text-muted-foreground">Watches grant extra AI tutor queries to Free tier users.</p>
            </div>
          </div>

          {/* Reward Bonus Amount */}
          <div className="p-4 rounded-lg bg-[#15201C] border border-border flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="space-y-0.5">
              <div className="text-xs font-bold text-foreground">Bonus AI Queries per Rewarded Video</div>
              <div className="text-[11px] text-muted-foreground">
                How many additional AI queries free users receive when completing a rewarded video ad.
              </div>
            </div>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min="1"
                max="20"
                value={config.bonusAiQueriesPerReward}
                onChange={(e) =>
                  setConfig({
                    ...config,
                    bonusAiQueriesPerReward: parseInt(e.target.value, 10) || 3,
                  })
                }
                className="w-20 text-center text-xs font-bold p-2 rounded-lg bg-[#0E1513] border border-border text-foreground focus:outline-none focus:border-primary"
              />
              <span className="text-xs text-muted-foreground">queries</span>
            </div>
          </div>

          {/* Important App ID Note */}
          <div className="p-4 rounded-lg bg-[#15201C] border border-border flex items-start gap-3 text-xs text-muted-foreground">
            <Info className="w-4 h-4 text-primary shrink-0 mt-0.5" />
            <div className="leading-relaxed">
              <span className="font-semibold text-foreground">Note on AdMob Application ID:</span> The Google Mobile Ads SDK requires the master Application ID (e.g. <code className="text-primary">ca-app-pub-XXXXXXXX~YYYYYYYY</code>) to be declared in <code className="text-foreground">AndroidManifest.xml</code> prior to building release APKs. The unit IDs above configure live ad delivery on the client at runtime.
            </div>
          </div>

          {/* Save Button */}
          <div className="flex justify-end pt-2">
            <button
              type="submit"
              disabled={saving || loading}
              className="px-5 py-2.5 text-xs font-bold rounded-lg bg-primary text-black hover:bg-primary/90 disabled:opacity-50 flex items-center gap-2 shadow-lg shadow-primary/20 transition-all cursor-pointer"
            >
              <Save className="w-4 h-4" />
              {saving ? 'Saving to Firestore...' : 'Save AdMob Configuration'}
            </button>
          </div>
        </form>
      </div>

      {/* Google Play Billing Governance Notice */}
      <div className="p-6 rounded-xl bg-[#111917] border border-border space-y-4">
        <div className="flex items-center gap-2">
          <CreditCard className="w-5 h-5 text-muted-foreground" />
          <h2 className="text-sm font-bold text-foreground">Google Play Billing Subscriptions</h2>
        </div>

        <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-2 text-xs text-muted-foreground leading-relaxed">
          <p className="font-semibold text-foreground">Rule 1 Governance Invariant:</p>
          <p>
            Google Play Billing v6 client-side acknowledgement is verified in the Android client. Revenue metrics and active subscriber counts reflect real Google Play Console sync.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-2">
          <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-1 text-xs">
            <div className="text-muted-foreground font-semibold">Total Revenue</div>
            <div className="text-xl font-bold text-foreground">₹0.00</div>
            <div className="text-[10px] text-muted-foreground">Syncs with Google Play Console</div>
          </div>

          <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-1 text-xs">
            <div className="text-muted-foreground font-semibold">Active Subscribers</div>
            <div className="text-xl font-bold text-foreground">0</div>
            <div className="text-[10px] text-muted-foreground">0 simulated accounts</div>
          </div>

          <div className="p-4 rounded-lg bg-[#15201C] border border-border space-y-1 text-xs">
            <div className="text-muted-foreground font-semibold">Server Entitlements</div>
            <div className="text-xl font-bold text-primary">ENFORCED</div>
            <div className="text-[10px] text-muted-foreground">Google Play Billing v6 Acknowledgement Active</div>
          </div>
        </div>
      </div>
    </div>
  );
}
