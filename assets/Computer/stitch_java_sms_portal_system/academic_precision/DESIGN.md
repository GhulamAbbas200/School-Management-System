---
name: Academic Precision
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#444653'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
  outline: '#757684'
  outline-variant: '#c4c5d5'
  surface-tint: '#3755c3'
  primary: '#00288e'
  on-primary: '#ffffff'
  primary-container: '#1e40af'
  on-primary-container: '#a8b8ff'
  inverse-primary: '#b8c4ff'
  secondary: '#006c4a'
  on-secondary: '#ffffff'
  secondary-container: '#82f5c1'
  on-secondary-container: '#00714e'
  tertiary: '#611e00'
  on-tertiary: '#ffffff'
  tertiary-container: '#872d00'
  on-tertiary-container: '#ffa583'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dde1ff'
  primary-fixed-dim: '#b8c4ff'
  on-primary-fixed: '#001453'
  on-primary-fixed-variant: '#173bab'
  secondary-fixed: '#85f8c4'
  secondary-fixed-dim: '#68dba9'
  on-secondary-fixed: '#002114'
  on-secondary-fixed-variant: '#005137'
  tertiary-fixed: '#ffdbce'
  tertiary-fixed-dim: '#ffb59a'
  on-tertiary-fixed: '#380d00'
  on-tertiary-fixed-variant: '#802a00'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  headline-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  data-tabular:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 4px
  sidebar-width: 260px
  gutter: 24px
  container-padding: 32px
  card-gap: 16px
---

## Brand & Style

The design system is engineered for the complex informational needs of educational institutions. It adopts a **Corporate / Modern** aesthetic that balances academic authority with contemporary software usability. The primary goal is to minimize cognitive load for administrators, teachers, and students by utilizing high-density layouts, clear visual hierarchies, and a restrained but purposeful color application.

The emotional response should be one of **stability, clarity, and competence**. By utilizing expansive white space in the content area contrasted against a structured, dark navigation environment, the design system establishes a professional workspace where data and statistics remain the primary focus.

## Colors

The palette is anchored by "Education Blue," a deep, authoritative primary color used for branding, primary actions, and active states. "Success Green" is reserved strictly for positive affirmations, grade improvements, and completed statuses.

*   **Primary (#1E40AF):** Used for primary buttons, focus states, and key navigational icons.
*   **Secondary (#059669):** Used for success messages, "Save" actions, and positive trend indicators in statistics.
*   **Surface:** The main workspace uses a pure white (#FFFFFF) background to ensure maximum text legibility, while the page backdrop uses a light slate (#F8FAFC) to define card boundaries.
*   **Navigation:** A dark sidebar (#0F172A) provides a high-contrast anchor for the application, housing the main site-map and reducing visual noise in the periphery.

## Typography

This design system utilizes **Inter** exclusively for its exceptional legibility in data-heavy environments. The typographic scale is optimized for information density.

For dashboards and tables, use `data-tabular` which utilizes tabular lining figures to ensure that numbers align vertically across rows, making grade and financial comparisons easier. Headers should remain concise, using a tighter letter-spacing for larger sizes to maintain a modern, "compact" feel.

## Layout & Spacing

The layout follows a **Fixed Sidebar / Fluid Content** model. The sidebar remains locked to the left, while the main content area expands to fill the viewport, utilizing a responsive 12-column grid system for internal card arrangements.

*   **Desktop:** 12-column grid with 24px gutters. Content is housed within cards that span 3, 4, 6, or 12 columns.
*   **Tablet:** 8-column grid with 16px gutters. Sidebar may collapse into an icon-only rail or a hamburger menu.
*   **Mobile:** Single column stack. Padding reduces to 16px on the container level.

Spacing follows a 4px baseline. Use 16px (base * 4) for component internal padding and 24px (base * 6) for standard vertical rhythm between sections.

## Elevation & Depth

Visual hierarchy is established through **Ambient Shadows** and **Tonal Layers**. Because the system is data-centric, elevation is kept minimal to avoid visual clutter.

*   **Level 0 (Background):** Slate #F8FAFC. Used for the main application backdrop.
*   **Level 1 (Cards):** Pure White #FFFFFF with a subtle shadow (0px 1px 3px rgba(0,0,0,0.1)). This is the standard surface for all dashboard widgets and data tables.
*   **Level 2 (Modals/Dropdowns):** Pure White #FFFFFF with a more pronounced shadow (0px 10px 15px -3px rgba(0,0,0,0.1)).
*   **Outlines:** Use a 1px border (#E2E8F0) for all form inputs and card boundaries to ensure definition on high-brightness displays.

## Shapes

The design system uses a **Soft** shape language. This creates a modern feel without sacrificing the professional rigor required of an academic portal.

*   **Small Components:** Buttons, inputs, and tags use a 0.25rem (4px) corner radius.
*   **Containers:** Dashboard cards and modals use a 0.5rem (8px) corner radius.
*   **Interactive Elements:** Focus indicators should mirror the roundedness of the parent element with a 2px offset.

## Components

### Buttons
*   **Primary:** Education Blue (#1E40AF) with white text. Solid fill.
*   **Success:** Success Green (#059669) with white text. Reserved for final submissions or approval actions.
*   **Ghost:** Transparent background with Primary Blue text and border. Used for secondary actions like "Cancel" or "Add Row".

### Form Controls
*   **Inputs:** White background, 1px border (#E2E8F0). On focus, the border shifts to Education Blue with a subtle 2px outer glow.
*   **Labels:** Use `body-md` in a Bold weight, placed 8px above the input field.

### Cards & Tables
*   **Cards:** Every dashboard widget must be contained in a white card with an 8px radius.
*   **Tables:** Use a "Zebra-stripe" pattern for rows (using #F8FAFC for alternate rows) to aid eye-tracking across wide data sets. Headers should have a light gray background (#F1F5F9).

### Navigation Sidebar
*   **Active State:** The active menu item should feature a left-aligned 4px "accent bar" in Education Blue and a slightly lighter background tint (#1E293B) to differentiate from the dark sidebar background.