# 0.16.0-rc.1 — Quick planting and a separate pond

Plant now behaves like Harvest: one tap fills all affordable empty unlocked beds with the selected seed, without a chooser, toast or pause. It preserves growing/ripe crops and debits only planted seeds. Individual beds still support planting and harvesting. Seasonal/day requirements and the six-bed starting farm plus 200-coin expansion remain unchanged.

Swipe down to hide Seeds, the bottom command buttons, construction/equipment panels and ordinary popups. Show controls restores the command dock. Horizontal seed scrolling and vertical content scrolling retain priority; scroll a panel to the top before pulling it down. Buttons and Close/Back remain available. Required recovery/progression choices and arena combat retain their explicit actions.

Fishing now lives in a separate visible pond below the south fence. All nine farm positions draw ordinary soil; the bottom three open through normal Farm expansion. The three-catch daily reward cap, timed reel, separate randomness and equipment odds are unchanged. Swiping the fishing popup closes it, cancels the cast and keeps the farm paused.

Wolves stay upright rather than rotating a side-view drawing through 360 degrees. They mirror toward travel/targets, and diagonal pairs of articulated legs animate a trot. Pause and Reduced motion retain static poses. No enemy stats or damage change.

Save schema 12 reads schemas 1–11. A schema11 farm that has crops in former floating beds, or has earned its three-catch quota, retains those beds as permanent ordinary farmland on upgrade. Crop positions, growth, health, resources, equipment and progression remain intact. New fishing catches never unlock farmland. Older apps reject schema12 exports: export before updating and keep the previous APK and pre-update backup for rollback.

Required candidate checks: debug/release JVM suite, Android builds/lint, package/audio/signature audits, installed-app regression and new real-pointer planting/swipe/pond checks on API26 and35, visual wolf/pond review, and same-key release upgrade retention. Physical-phone performance, accessibility, endurance and human balance validation remain pending. This is a prerelease, not a stable promotion.
