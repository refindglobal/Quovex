import { describe, it } from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import { ASSETS } from '../lib/assets';

describe('Quovex Asset Architecture & Integrity Tests', () => {
  const publicDir = path.join(__dirname, '../public');

  it('Brand Assets: All brand logos, wordmarks, and emblems exist and are non-empty', () => {
    const brandEntries = [
      ASSETS.brand.logo,
      ASSETS.brand.logoText,
      ASSETS.brand.logoBg,
      ASSETS.brand.emblem,
      ASSETS.brand.heroMockup,
    ];

    for (const relPath of brandEntries) {
      const fullPath = path.join(publicDir, relPath);
      assert.ok(fs.existsSync(fullPath), `Brand asset missing: ${relPath}`);
      const stat = fs.statSync(fullPath);
      assert.ok(stat.size > 1000, `Brand asset too small/corrupt: ${relPath} (${stat.size} bytes)`);
    }
  });

  it('Scholar Avatars: All 12 avatars (1–12) exist and are non-empty PNGs', () => {
    for (let id = 1; id <= 12; id++) {
      const relPath = ASSETS.avatars(id);
      const fullPath = path.join(publicDir, relPath);
      assert.ok(fs.existsSync(fullPath), `Avatar ${id} missing: ${relPath}`);
      const stat = fs.statSync(fullPath);
      assert.ok(stat.size > 10000, `Avatar ${id} corrupt: ${relPath}`);
    }
  });

  it('Deck Artworks: All 5 subject deck backgrounds exist', () => {
    const deckEntries = Object.values(ASSETS.decks);
    assert.equal(deckEntries.length, 5);

    for (const relPath of deckEntries) {
      const fullPath = path.join(publicDir, relPath);
      assert.ok(fs.existsSync(fullPath), `Deck background missing: ${relPath}`);
      const stat = fs.statSync(fullPath);
      assert.ok(stat.size > 50000, `Deck background corrupt: ${relPath}`);
    }
  });

  it('System Illustrations: Empty deck, empty notes, blocker, permissions, welcome SVGs exist', () => {
    const illEntries = Object.values(ASSETS.illustrations);
    assert.equal(illEntries.length, 5);

    for (const relPath of illEntries) {
      const fullPath = path.join(publicDir, relPath);
      assert.ok(fs.existsSync(fullPath), `Illustration missing: ${relPath}`);
      const stat = fs.statSync(fullPath);
      assert.ok(stat.size > 500, `Illustration corrupt: ${relPath}`);
    }
  });

  it('3D Icons: All 3D icons across Ranks, Timer, AI, Flashcards, Streaks, Blocker, Planner, and Subjects exist', () => {
    const iconEntries = Object.values(ASSETS.icons3d);
    assert.ok(iconEntries.length >= 35, 'Must have at least 35 3D icon tokens');

    for (const relPath of iconEntries) {
      const fullPath = path.join(publicDir, relPath);
      assert.ok(fs.existsSync(fullPath), `3D icon missing on disk: ${relPath}`);
      const stat = fs.statSync(fullPath);
      assert.ok(stat.size > 5000, `3D icon corrupt: ${relPath}`);
    }
  });

  it('Parity: quovex-admin public assets match quovex-web public assets', () => {
    const adminPublicDir = path.join(__dirname, '../../quovex-admin/public');
    assert.ok(fs.existsSync(adminPublicDir), 'quovex-admin public directory must exist');

    for (let id = 1; id <= 12; id++) {
      const relPath = ASSETS.avatars(id);
      const adminPath = path.join(adminPublicDir, relPath);
      assert.ok(fs.existsSync(adminPath), `Admin missing avatar ${id}: ${relPath}`);
    }

    const brandEmblemPath = path.join(adminPublicDir, ASSETS.brand.emblem);
    assert.ok(fs.existsSync(brandEmblemPath), 'Admin missing brand emblem');
  });
});
