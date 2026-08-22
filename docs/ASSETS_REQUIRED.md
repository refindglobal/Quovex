# Quovex — Complete Asset Requirements (Checklist)

This document lists every single image, icon, animation, sound, and font file required to build the Quovex Android app based on our technical deep dive and UI specifications. 

*Recommendation:* Use **SVG** (converted to Android `VectorDrawable` XML) for all icons and illustrations to keep the app size small. Use **WebP** for raster images (avatars, deck covers).

---

## 1. 📝 Typography (Fonts)
Place in: `res/font/`
- [ ] `inter_regular.ttf` (Body text)
- [ ] `inter_medium.ttf` (Buttons, Labels)
- [ ] `inter_semibold.ttf` (Headings)
- [ ] `inter_bold.ttf` (Large numbers, Timer digits)

---

## 2. 🚀 App Branding & Launcher
Place in: `res/mipmap/` and `res/drawable/`
- [ ] `ic_launcher_background.xml` (Solid #0A1F16 or gradient)
- [ ] `ic_launcher_foreground.xml` (The "Q" lightning logo)
- [ ] `ic_launcher_monochrome.xml` (For Android 13+ themed icons)
- [ ] `ic_quovex_logo_full.svg` (Text + Icon for Splash Screen)

---

## 3. 📱 Navigation Icons (Bottom Bar)
*Note: You need 2 versions for each — one outlined (inactive) and one filled (active).*
- [ ] `ic_nav_home_outline.xml` & `ic_nav_home_filled.xml`
- [ ] `ic_nav_timer_outline.xml` & `ic_nav_timer_filled.xml`
- [ ] `ic_nav_library_outline.xml` & `ic_nav_library_filled.xml`
- [ ] `ic_nav_community_outline.xml` & `ic_nav_community_filled.xml`
- [ ] `ic_nav_profile_outline.xml` & `ic_nav_profile_filled.xml`

---

## 4. 🎨 Onboarding & Empty State Illustrations
- [ ] `ill_welcome.svg` (Welcome to Quovex graphic)
- [ ] `ill_permissions.svg` (Graphic asking for notification/camera access)
- [ ] `ill_empty_notes.svg` ("No notes yet, let's scan one!")
- [ ] `ill_empty_deck.svg` ("All caught up on flashcards!")
- [ ] `ill_focus_blocked.svg` (Graphic shown when distracting app is blocked)

---

## 5. 🧑‍🎓 Avatars & User Profiles
Place in: `res/drawable/` (Preferably WebP)
- [ ] `avatar_1.webp` through `avatar_12.webp` (Preset avatars for users to pick)
- [ ] `ic_premium_crown.xml` (Gold badge for premium users)
- [ ] `ic_streak_flame.xml` (Fire icon for streak counts)

---

## 6. 📚 Exams & Subject Thumbnails
Used during onboarding and to label Study Rooms/Decks.
- [ ] **Exams:** `ic_exam_jee.svg`, `ic_exam_neet.svg`, `ic_exam_sat.svg`, `ic_exam_boards.svg`, `ic_exam_ielts.svg`
- [ ] **Subjects:** `ic_sub_physics.svg`, `ic_sub_chemistry.svg`, `ic_sub_maths.svg`, `ic_sub_biology.svg`, `ic_sub_history.svg`
- [ ] **Deck Covers:** High-res WebP background images for Flashcard Decks (e.g., `deck_physics_bg.webp`).

---

## 7. 🎬 Lottie Animations (.json)
Place in: `res/raw/` (Used with `lottie-compose` library)
- [ ] `anim_ai_thinking.json` (Sparkles/Pulse while Groq is generating)
- [ ] `anim_streak_fire.json` (Animated fire for streak celebration)
- [ ] `anim_confetti.json` (When a study plan or deck is completed)
- [ ] `anim_timer_ring.json` (Smooth breathing pulse for focus mode)
- [ ] `anim_scan_line.json` (Scanner line moving up/down when taking a photo of a note)

---

## 8. 🔊 Audio Effects (Sound Design)
Place in: `res/raw/` (Keep under 50kb each, use `.ogg` format)
- [ ] `snd_timer_tick.ogg` (Subtle haptic-like tick for the last 10 seconds of timer)
- [ ] `snd_card_flip.ogg` (Paper swipe sound for flashcards)
- [ ] `snd_success.ogg` (Pleasant chime when finishing a session)
- [ ] `snd_alert.ogg` (Warning beep when ML Kit detects eyes closed / focus lost)

---

## 9. 🤖 AI Chat & Action Icons
- [ ] `ic_ai_sparkle.xml` (To indicate AI actions like "Summarize" or "Explain")
- [ ] `ic_send.xml` (Chat send button)
- [ ] `ic_camera_scan.xml` (Floating Action Button for image doubt solver)
- [ ] `ic_upload_pdf.xml` (For notes summarizer)
- [ ] `ic_link.xml` (For pasting YouTube/Web URLs)
- [ ] `ic_groq_badge.xml` & `ic_cerebras_badge.xml` (To show which AI model generated the content, if desired for debug/transparency)
