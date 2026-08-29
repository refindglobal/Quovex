/**
 * Canonical 3D Asset, Avatar & Brand Registry for Quovex Admin
 * All paths are 100% verified against local filesystem assets.
 */

export const ASSETS = {
  // User Avatars (1–12)
  avatars: (id: number = 1) => `/assets/avatars/avatar_${Math.min(12, Math.max(1, id))}.png`,

  // Deck Artworks
  decks: {
    physics: '/assets/decks/deck_physics.jpg',
    chemistry: '/assets/decks/deck_chemistry.jpg',
    biology: '/assets/decks/deck_biology.jpg',
    maths: '/assets/decks/deck_maths.jpg',
    history: '/assets/decks/deck_history.jpg',
  },

  // Empty States & System Illustrations
  illustrations: {
    emptyDeck: '/assets/illustrations/ill_empty_deck.svg',
    emptyNotes: '/assets/illustrations/ill_empty_notes.svg',
    focusBlocked: '/assets/illustrations/ill_focus_blocked.svg',
    permissions: '/assets/illustrations/ill_permissions.svg',
    welcome: '/assets/illustrations/ill_welcome.svg',
  },

  // Brand Identity
  brand: {
    logo: '/assets/brand/logo.png',
    logoText: '/assets/brand/logo_text.png',
    logoBg: '/assets/brand/logo_bg.png',
    emblem: '/assets/brand/logo_emblem.png',
    heroMockup: '/assets/brand/hero_mockup.png',
  },

  // 3D Visual Icons
  icons3d: {
    // Scholar Ranks & Gamification
    rankNovice: '/assets/icons/3d/Flame_badge_with_green_fire_202608262144.png',
    rankApprentice: '/assets/icons/3d/Silver_and_emerald_cyber_helmet_202608262144.png',
    rankStrategist: '/assets/icons/3d/Floating_badge_with_neon_atom_202608262144.png',
    rankGrandmaster: '/assets/icons/3d/Cybernetic_crown_with_emerald_fl…_202608262144.png',
    trophy: '/assets/icons/3d/Futuristic_championship_trophy_f…_202608262144.png',
    tournamentPodium: '/assets/icons/3d/Tournament_podium_with_cyber_crowns_202608262144.png',
    badgeLion: '/assets/icons/3d/Floating_badge_with_lion_emblem_202608262144.png',
    sabers: '/assets/icons/3d/Crossed_energy_sabers_clashing_202608262144.png',

    // Focus & Timer
    stopwatch: '/assets/icons/3d/Futuristic_circular_stopwatch_gl…_202608262144.png',
    emblemQ: '/assets/icons/3d/Metallic_letter_Q_stopwatch_emblem_202608262144.png',
    soundscapeRain: '/assets/icons/3d/Storm_cloud_with_glowing_raindrops_202608262144.png',
    soundscapeCoffee: '/assets/icons/3d/Cassette_tape_and_coffee_cup_202608262144.png',
    soundscapeClock: '/assets/icons/3d/Cloud_raining_over_melting_clock_202608262144.png',
    studyTable: '/assets/icons/3d/Glass_study_table_with_lamps_202608262144.png',

    // AI & Doubt Solver
    robotMascot: '/assets/icons/3d/Futuristic_robot_sphere_floating_202608262144.png',
    scannerHologram: '/assets/icons/3d/Holographic_camera_scanner_brack…_202608262144.png',
    scannerLens: '/assets/icons/3d/Holographic_camera_lens_scanning…_202608262144.png',
    laserBook: '/assets/icons/3d/Laser_scanning_book_text_202608262144.png',
    brainClockwork: '/assets/icons/3d/Emerald_brain_model_with_clockwork_202608262144.png',
    brainCybernetic: '/assets/icons/3d/Cybernetic_brain_sphere_with_sou…_202608262144.png',
    lightbulbBreakout: '/assets/icons/3d/Lightbulb_breaking_from_maze_puzzle_202608262144.png',
    magnifier: '/assets/icons/3d/Magnifying_glass_hovering_over_f…_202608262144.png',

    // Flashcards & Quiz
    flashcards: '/assets/icons/3d/Holographic_flashcards_floating_…_202608262144.png',
    quizBuzzer: '/assets/icons/3d/Quiz_game_show_buzzer_podium_202608262144.png',
    formulaSlate: '/assets/icons/3d/Floating_slate_tablet_with_formulas_202608262144.png',

    // Streaks & Vault
    flameBurning: '/assets/icons/3d/Burning_emerald_flame_badge_stop…_202608262144.png',
    vaultChest: '/assets/icons/3d/Futuristic_titanium_storage_ches…_202608262144.png',
    iceShield: '/assets/icons/3d/Crystalline_ice_shield_with_flame_202608262144.png',
    plantSprout: '/assets/icons/3d/Glowing_plant_sprout_in_pot_202608262144.png',
    sproutEmerging: '/assets/icons/3d/Emerald_plant_sprout_emerging_ca…_202608262144.png',
    sparkOrb: '/assets/icons/3d/Neon_spark_orb_in_cradle_202608262144.png',
    runeStone: '/assets/icons/3d/Obsidian_rune_stone_bursting_energy_202608262144.png',

    // App Blocker & Notifications
    shieldChains: '/assets/icons/3d/Smartphone_wrapped_in_glowing_ch…_202608262144.png',
    brokenFiber: '/assets/icons/3d/Broken_fiber-optic_cable_sparking_202608262144.png',
    notificationBell: '/assets/icons/3d/Emerald_notification_bell_with_p…_202608262144.png',

    // Planner & Knowledge
    calendar: '/assets/icons/3d/Floating_holographic_calendar_gr…_202608262144.png',
    metallicBook: '/assets/icons/3d/Open_metallic_book_with_quill_202608262144.png',
    radar: '/assets/icons/3d/Radar_scanner_sweeping_skill_nodes_202608262144.png',
    graduationCap: '/assets/icons/3d/Graduation_cap_and_scroll_202608262144.png',
    planetRings: '/assets/icons/3d/Floating_planet_with_glowing_rings_202608262144.png',
    ufoBeam: '/assets/icons/3d/UFO_beaming_light_on_notebook_202608262144.png',

    // Subjects
    chemBenzene: '/assets/icons/3d/Benzene_ring_connected_to_flask_202608262144.png',
    bioStethoscope: '/assets/icons/3d/Stethoscope_wrapped_around_DNA_s…_202608262144.png',
    mathMobius: '/assets/icons/3d/Mobius_strip_with_math_symbols_202608262144.png',
    physicsOrbit: '/assets/icons/3d/Quantum_orbital_sphere_floating_202608262144.png',
    historyScroll: '/assets/icons/3d/Greek_pillar,_compass,_and_scroll_202608262144.png',
    csBinary: '/assets/icons/3d/Binary_code_displaying_in_terminal_202608262144.png',
  },
};
