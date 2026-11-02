# 15. Page layout and theme

Status: not started.

The page gets a designed layout and theme around the world, light and dark. The
world comes first. A bar along the top holds the play controls, the zoom and the
panel buttons, and stays visible. A rail on the left holds the tools. The
inspector, the creature table, the population chart, the settings and the world
actions open on demand in a drawer that pushes the world aside, so nothing
covers it. The drawer shows its panel's name and a close button, and the bar's
buttons are the only way to switch panels.

The tools are Inspect, which is selected by default, a hand that pans, and Place,
which opens a list of species defaulting to the first. An armed Place tool shows
a ghost sprite under the cursor and each click places one, and Esc, a right
click or the tool's button again goes back to Inspect. This replaces entry 13's
inspect and spawn toggle, which worked and was hard to follow.

The world opens at the smallest whole scale that fills the window's shorter
side, centred. Buttons, the mouse wheel at the cursor and the plus and minus keys
zoom it in whole steps, and Fit returns to the opening scale. A drag pans with
any tool, and a press that moves under 4 pixels stays a click. The meadow
continues past the world's edge, dimmed, behind a thin border.

The theme follows the system setting, and a sun and moon toggle in the bar
overrides it and is remembered in that browser. Only the page goes dark. The
world keeps its daylight colours.

These choices came out of entry 14's grill, tried on a live page with the
entry's art. The layout was picked from three options made with the
`frontend-design` skill, and the same skill guides the build.

Details to settle in this entry's grill: whether the picker and the create
screen get the same treatment, whether
[world size on the page](../ideas/world-size-on-the-page.md) joins in, the
keyboard shortcuts, and how the panels behave on a narrow window.

On the page: everything above.

Depends on entry 14. See
[0020](../decisions/0020-the-interface-is-a-web-page.md).
