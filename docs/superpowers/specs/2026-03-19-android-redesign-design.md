# Hackers'Pub Android — Full UI Redesign

**Date:** 2026-03-19
**Status:** Approved
**Goal:** Replace the default Material Design 3 styling with a custom, Ivory-inspired design system that gives Hackers'Pub a distinctive, editorial identity across both light and dark themes.

## Design Philosophy

Platform-agnostic custom identity — not Material, not iOS-native, but a unique Hackers'Pub look inspired by Ivory (Tapbots' Mastodon client). Near-monochrome palette with Deep Stone accent. Typography-first: font weight carries emphasis rather than color. Semantic colors (red, green) reserved for interactive state changes, giving them maximum impact against the neutral base.

## Design System

### Color Palette

#### Light Theme
| Token | Value | Usage |
|-------|-------|-------|
| `background` | `#FFFFFF` | Screen background |
| `surface` | `#F2F2F7` | Dividers, secondary surfaces, icon button backgrounds |
| `textPrimary` | `#1C1C1E` | Headlines, names, primary content |
| `textBody` | `#3A3A3C` | Body text, post content |
| `textSecondary` | `#AEAEB2` | Handles, timestamps, inactive tab labels |
| `accent` | `#44403C` | Active tab icons/labels, links (via weight), follow buttons, interactive elements |
| `accentMuted` | `#78716C` | Hashtags, relationship tags background at 10% opacity |
| `divider` | `#F2F2F7` | Hairline separators between posts |
| `buttonOutline` | `#D6D3D1` | Secondary button borders |
| `error` / `reaction` | `#E8453C` | Heart reactions, error states, notification dots |
| `share` | `#34D399` | Share/repost active state |

#### Dark Theme
| Token | Value | Usage |
|-------|-------|-------|
| `background` | `#171717` | Screen background |
| `surface` | `#262626` | Dividers, secondary surfaces, icon button backgrounds |
| `textPrimary` | `#FAFAFA` | Headlines, names |
| `textBody` | `#D4D4D4` | Body text |
| `textSecondary` | `#737373` | Handles, timestamps, inactive tabs |
| `accent` | `#D6D3D1` | Active tab icons/labels, interactive elements (inverted stone) |
| `accentMuted` | `#A8A29E` | Hashtags, tags |
| `divider` | `#262626` | Hairline separators |
| `buttonOutline` | `#525252` | Secondary button borders |
| `error` / `reaction` | `#E8453C` | Same as light |
| `share` | `#34D399` | Same as light |

### Typography

System default (Roboto on Android). No custom fonts — emphasis via weight hierarchy only.

| Style | Size | Weight | Usage |
|-------|------|--------|-------|
| `titleLarge` | 22sp | 700 (Bold) | Screen titles (Timeline, Explore, etc.) |
| `titleMedium` | 20sp | 700 (Bold) | Profile name |
| `bodyLarge` | 15sp | 400 (Regular) | Post content, bio text |
| `bodyMedium` | 14sp | 400 (Regular) | Stats labels, settings items |
| `labelMedium` | 13sp | 400 (Regular) | Handles, timestamps, engagement counts |
| `labelSmall` | 12sp | 500 (Medium) | Hashtags, relationship tags |
| `caption` | 11sp | 500 (Medium) | Tag pills, repost indicators |
| `tabLabel` | 10sp | 600 (SemiBold) | Bottom tab labels |

**Letter spacing:** -0.3sp on `titleLarge` for tighter headline feel. Default elsewhere.

### Spacing

Base unit: 4dp. Consistent throughout.

| Token | Value | Usage |
|-------|-------|-------|
| `xs` | 4dp | Fine spacing (between name and handle) |
| `sm` | 8dp | Compact spacing (engagement icon gaps) |
| `md` | 12dp | Element spacing (avatar-to-text gap, between post sections) |
| `lg` | 16dp | Standard padding (screen edges, post padding) |
| `xl` | 24dp | Section spacing (stats row gaps) |

### Shapes

| Element | Radius | Size |
|---------|--------|------|
| Avatar (timeline) | Circle | 42dp |
| Avatar (profile) | Circle | 80dp |
| Avatar (repost indicator) | Circle | 16dp |
| Pill buttons (Follow, etc.) | 20dp | Height ~36dp |
| Icon buttons (compose, etc.) | Circle | 28dp |
| Relationship tags | Full capsule | Auto-width |
| Notification dot | Circle | 8dp |

No card elevation or shadows anywhere. Post separation via hairline dividers only.

### Icons

Outlined stroke icons (Feather/Lucide style), 22dp for tab bar, 16dp for engagement bar. Stroke width 1.5dp.

- Active state: filled icon with accent color
- Inactive state: outlined icon with secondary color
- Engagement active states: filled with semantic color (red heart, green repost)

## Screen Specifications

### Timeline Screen

**Header:**
- Large bold title "Timeline" (22sp/700) top-left
- Right side: compose button (28dp circle, surface background, + icon) and user avatar (28dp circle)
- No border below header — content starts immediately

**Content:**
- Vertical scrolling list (LazyColumn)
- Posts separated by 1dp hairline dividers (`divider` color) with 16dp horizontal margin
- Pull-to-refresh support

**Post Card Layout:**
- 16dp horizontal padding, 12dp vertical padding
- Repost indicator (if shared): 16dp avatar + repost icon + actor name, caption size, secondary color, indented to align with post content (48dp left padding)
- Author row: 42dp circular avatar, 12dp gap, then name (15sp/600) + timestamp (13sp/secondary) on same baseline
- Body text: 15sp/400, `textBody` color, 4dp below author row
- Hashtags: 11sp/500, `accentMuted` color, 6dp below body
- Engagement bar: 10dp below content, icons (16dp) + counts (13sp), 18dp gaps between items. Muted by default, semantic colors when active (red heart filled, green repost)

**Quoted Post (if present):**
- Indented block below body text
- 1dp border in `divider` color, 8dp border-radius, 8dp padding
- Smaller avatar (32dp), author info, truncated content

**Floating Action Button:** None — compose is in the header

### Profile Screen

**Header:** Back arrow (left) + ellipsis menu (right), no title text

**Profile Section (centered):**
- 80dp circular avatar
- Name: 20sp/700, 12dp below avatar
- Handle: 14sp/400, `textSecondary`, 2dp below name
- Bio: 14sp/400, `textBody`, centered, 10dp below handle, 24dp horizontal margin
- Stats row: 24dp gap between items, centered, 14dp below bio
  - Each stat: count (16sp/700) above label (12sp/secondary)
  - Three items: Posts, Followers, Following
- Action buttons: 16dp below stats, centered
  - Follow: pill button, `accent` background, white text, 28dp horizontal padding
  - Share: pill button, transparent, `buttonOutline` border, share icon only
- Relationship tag: "Follows you" capsule, `accentMuted` at 10% opacity background, 11sp/500, 10dp below buttons

**Content:** Hairline divider, then post list identical to timeline (without avatars if showing own profile — keep avatars for consistency)

### Notifications Screen

**Header:** Large bold title "Notifications" (same as timeline)

**Notification Rows:**
- 42dp actor avatar (circular), 12dp gap
- Action text: actor name (bold) + action description + timestamp
- Notification type indicated by subtle icon or text ("liked your post", "shared your post", "followed you")
- Hairline dividers between items
- Unread indicator: red dot (8dp) on the Alerts tab icon
- Empty state: centered text "No notifications yet"

### Explore Screen

**Header:** Large bold title "Explore"

**Segmented Tabs:** Below header, "Local" / "Global" segmented control
- Active tab: `accent` text, underline indicator
- Inactive tab: `textSecondary`

**Content:** Same post list as timeline, scoped to local or global feed

### Search Screen

**Header:** Large bold title "Search"

**Search Bar:** Rounded text field below header, `surface` background, 12dp border-radius, search icon left-aligned, 16dp horizontal margin

**Results:** Actor rows (avatar + name + handle) and post cards, depending on result type

### Compose Screen

**Header:** Navigation bar style
- Left: "Cancel" text button (`accent` color)
- Right: "Post" pill button (`accent` background, white text), disabled state at 40% opacity
- Title: "New Post" or "Reply" centered (17sp/600)

**Content:**
- Reply indicator (if replying): "Replying to @handle" in `textSecondary`, 13sp
- Text editor: full-width, 15sp, placeholder "What's on your mind?"
- Markdown support with live preview toggle

**Bottom toolbar:** Visibility selector, language picker — subtle icons in `textSecondary`

### Settings Screen

**Header:** Large bold title "Settings"

**Content:** Grouped list sections with section headers (13sp/500, uppercase, `textSecondary`)

Sections:
- **App Info:** Logo (80dp, 16dp corner radius), app name, version
- **Typography:** Font size slider (0.75x–2.0x)
- **Timeline:** Markdown truncation options
- **Engagement:** Confirmation toggles
- **Links:** In-app browser toggle
- **App Icons:** Alternative icon selection

Each row: standard list item, 16dp padding, `textPrimary` label, `textSecondary` value/description. Hairline dividers between rows.

### Sign-In Screen

**Layout:** Centered vertically

- Logo: 120dp, centered
- Title: "Hackers' Pub" (22sp/700), 16dp below logo
- Subtitle: "Sign in to continue" (14sp, `textSecondary`), 4dp below title
- Username field: rounded border (12dp radius), `surface` background, 32dp below subtitle, 32dp horizontal margin
- Verification code field: same style (appears after username submission)
- Primary button: pill shape, `accent` background, white text, full width within margins
- Error text: `error` color, 13sp, below button

## Migration Strategy

This redesign replaces the existing Material Design 3 theming while keeping the same Compose architecture. Changes are purely in the UI layer:

1. **Theme.kt** — Replace M3 color scheme with custom color tokens
2. **Spacing.kt** — Already exists, minor adjustments to values
3. **New: Typography.kt** — Define custom text styles (not M3 Typography)
4. **New: Shapes.kt** — Define shape tokens
5. **New: Icons.kt** — Custom icon set (if switching from Material Icons to Feather/Lucide)
6. **Each screen** — Replace M3 components (TopAppBar, FloatingActionButton, etc.) with custom composables
7. **PostCard** — Restyle engagement bar, remove card elevation, update spacing
8. **CompactTopBar** — Replace with new LargeTitle header composable

No changes to ViewModels, repositories, data layer, or navigation graph. Pure visual transformation.

## Out of Scope

- Custom animations/transitions (can be added later)
- Custom font families (using system Roboto)
- Tablet/landscape layouts
- Widget design
- Onboarding flow
