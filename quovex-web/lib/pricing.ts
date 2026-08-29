export interface PricingPlan {
  id: string;
  name: string;
  badge?: string;
  monthlyPrice: string;
  annualPrice: string;
  annualFormatted: string;
  trialDays?: number;
  savingsPct?: number;
  features: string[];
}

export interface RegionPricing {
  currency: string;
  symbol: string;
  monthly: number;
  annual: number;
  lifetime: number;
  gateway: 'razorpay' | 'lemonsqueezy';
}

export const REGIONAL_PPP: Record<string, RegionPricing> = {
  IN: { currency: 'INR', symbol: '₹', monthly: 199, annual: 999, lifetime: 2499, gateway: 'razorpay' },
  US: { currency: 'USD', symbol: '$', monthly: 4.99, annual: 34.99, lifetime: 89.99, gateway: 'lemonsqueezy' },
  GB: { currency: 'GBP', symbol: '£', monthly: 3.99, annual: 27.99, lifetime: 69.99, gateway: 'lemonsqueezy' },
  EU: { currency: 'EUR', symbol: '€', monthly: 4.49, annual: 29.99, lifetime: 79.99, gateway: 'lemonsqueezy' },
  GLOBAL: { currency: 'USD', symbol: '$', monthly: 4.99, annual: 34.99, lifetime: 89.99, gateway: 'lemonsqueezy' },
};

export function getPricingForCountry(countryCode: string = 'IN'): RegionPricing {
  return REGIONAL_PPP[countryCode.toUpperCase()] || REGIONAL_PPP.GLOBAL;
}
