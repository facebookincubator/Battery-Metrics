---
id: mistrustosbattery
title: Mistrusting OS Level Battery Drain
---

In a perfect, engineer-friendly world, we would be able to precisely determine the energy consumed by our application in production with a breakdown allocating energy to different hardware components and even source code, independent of physical conditions.

Unfortunately, the only metric we have access to consistently is the current battery level reported by Android, which has several limitations:

- Total energy drain depends on a lot more than just the app we care about:
  - the phone screen itself tends to dominate energy consumption (unless we're doing something very bad, like a busy loop – or keeping the camera open unnecessarily); variations in energy consumption don't really show up against the cost of the screen. This also varies with the brightness which depends on ambient light.
  - any app running on the phone is going to contribute to energy consumption, which is particularly significant when we're considering background sessions.
- Real world battery consumption also depends a lot on physical conditions: the signal strength, the temperature, what rate the current is being drained at, etc. These aren't things we can directly affect or control and just add noise if evaluated.
- It's a very coarse metric: we only have access to the percentage, so it only goes from 0-100 in steps of 1. It's fairly rare for that to move within a single session.
- The reported battery level is smoothed out for a better experience: you can keep charging the phone at 100% and it'll last longer than you'd expect at 1% which makes it more untrustworthy.

Seeing battery drain move is sufficient to determine that a given feature caused a regression – but not seeing battery drain move is not sufficient to assume that a feature is safe to ship.

This is why we don't rely so much on trying to see how fast the battery level is dropping and rely on utilization measurement instead.
