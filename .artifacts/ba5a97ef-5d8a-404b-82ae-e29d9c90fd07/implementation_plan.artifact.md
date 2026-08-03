# Implementation Plan: Redesign About Screen (Shadow Monarch Aesthetic)

Redesign the "About" screen to match the Shadow Monarch aesthetic, updating the branding to "eXork" and refining the layout and content.

## Proposed Changes

### [Screens]

#### [MODIFY] [AboutScreen.kt](file:///D:/MY_APPLICATION/app/src/main/java/com/example/myapplication/ui/screens/AboutScreen.kt)

- **Branding Update**:
    - Replace the "⚡" icon with the app's monarch launcher icon (`R.mipmap.ic_launcher_monarch`).
    - Change the app title to "eXork" with a strong Electric Cyan glow.
    - Update version text to "Version 3.1.0 - SYSTEM ACTIVE".
- **Mission Card**:
    - Update content: "An advanced Hunter RPG interface powered by eXork system protocols. Level up, complete daily quests, earn achievements, and rise through the hunter ranks."
    - Ensure 1.5dp Electric Cyan border and Translucent Slate glass look.
- **Section: THE ARCHITECTS**:
    - Stylize the credit items for "OM KRISHALI" (Developer) and "Ashu [ Player E1 ]" (Tester).
    - Use the Shadow Monarch glass theme for these items.
- **Section: CONNECT**:
    - Stylize social link items for GitHub (@AlgoBugSquasher) and Instagram (@omkrishali).
    - Ensure row cards have sleek glass styling and clickable launchers.
- **Footer**:
    - Update and stylize: "© 2026 OM KRISHALI".

## Verification Plan

### Manual Verification
1. **Visual Audit**:
    - Verify the new icon and "eXork" title rendering on the About screen.
    - Confirm the version text and mission description updates.
    - Check the styling of "THE ARCHITECTS" and "CONNECT" sections.
2. **Interactive Check**:
    - Verify that social links still open correctly in the browser.
    - Ensure the back button and click sounds are functional.
3. **Consistency**:
    - Ensure the entire screen uses the MonarchSlate/ElectricCyan palette.
