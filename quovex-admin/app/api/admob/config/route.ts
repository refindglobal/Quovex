import { NextResponse } from 'next/server';
import { getAdminFirestore } from '@/lib/firebase-admin';
import { verifyAdminSession } from '@/lib/auth/rbac';
import { adminStore } from '@/lib/admin-store';

export interface AdMobConfigData {
  bannerAdUnitId: string;
  interstitialAdUnitId: string;
  rewardedAdUnitId: string;
  adsEnabled: boolean;
  bonusAiQueriesPerReward: number;
  updatedAt?: number;
  updatedBy?: string;
}

const DEFAULT_ADMOB_CONFIG: AdMobConfigData = {
  bannerAdUnitId: 'ca-app-pub-3940256099942544/6300978111',
  interstitialAdUnitId: 'ca-app-pub-3940256099942544/1033173712',
  rewardedAdUnitId: 'ca-app-pub-3940256099942544/5224354917',
  adsEnabled: true,
  bonusAiQueriesPerReward: 3,
};

export async function GET(req: Request) {
  try {
    const auth = verifyAdminSession(req, 'VIEW_ANALYTICS');
    if (!auth.authorized) {
      return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
    }

    const db = getAdminFirestore();
    const docSnap = await db.collection('config').doc('admob').get();

    if (docSnap.exists) {
      const data = docSnap.data() as AdMobConfigData;
      return NextResponse.json({
        success: true,
        config: { ...DEFAULT_ADMOB_CONFIG, ...data },
      });
    }

    return NextResponse.json({
      success: true,
      config: DEFAULT_ADMOB_CONFIG,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to fetch AdMob config' },
      { status: 500 }
    );
  }
}

export async function POST(req: Request) {
  try {
    const auth = verifyAdminSession(req, 'MANAGE_SETTINGS');
    if (!auth.authorized) {
      return NextResponse.json({ error: auth.error }, { status: auth.statusCode || 401 });
    }

    const body = (await req.json()) as Partial<AdMobConfigData>;

    const updatedConfig: AdMobConfigData = {
      bannerAdUnitId: body.bannerAdUnitId?.trim() || DEFAULT_ADMOB_CONFIG.bannerAdUnitId,
      interstitialAdUnitId: body.interstitialAdUnitId?.trim() || DEFAULT_ADMOB_CONFIG.interstitialAdUnitId,
      rewardedAdUnitId: body.rewardedAdUnitId?.trim() || DEFAULT_ADMOB_CONFIG.rewardedAdUnitId,
      adsEnabled: body.adsEnabled !== undefined ? Boolean(body.adsEnabled) : true,
      bonusAiQueriesPerReward: typeof body.bonusAiQueriesPerReward === 'number' ? body.bonusAiQueriesPerReward : 3,
      updatedAt: Date.now(),
      updatedBy: auth.admin?.email || 'admin',
    };

    const db = getAdminFirestore();
    await db.collection('config').doc('admob').set(updatedConfig, { merge: true });

    // Record audit log
    await adminStore.recordAuditLog({
      action: 'UPDATE_ADMOB_CONFIG',
      performedBy: auth.admin?.email || 'admin',
      target: 'config/admob',
      details: `Updated AdMob config: adsEnabled=${updatedConfig.adsEnabled}, banner=${updatedConfig.bannerAdUnitId.slice(0, 15)}...`,
    });

    return NextResponse.json({
      success: true,
      message: 'AdMob configuration successfully saved to Firestore.',
      config: updatedConfig,
    });
  } catch (error: any) {
    return NextResponse.json(
      { success: false, error: error.message || 'Failed to save AdMob config' },
      { status: 500 }
    );
  }
}
