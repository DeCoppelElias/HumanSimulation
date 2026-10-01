# Brain memory

Status: idea.

## What it does

Lets a brain carry state from one day to the next, such as the food it chose to
walk towards. The 2022 humans did this: once a human picked a food target it
kept walking to it until the food was gone, instead of choosing again every
day.

## Why it is interesting

A brain that forgets everything overnight can only react to what it sees right
now. Memory allows commitment, such as heading for a far patch without being
distracted, and later things like returning home. It also changes what a
mutation can discover, since a remembered plan is a behaviour of its own.

## What it would touch

The brain is a component, per
[0003](../decisions/0003-entities-carry-components.md), so it could hold fields.
That state is per individual and is not a gene, so it is not inherited, and
[0013](../decisions/0013-perception-is-one-type.md) gives a brain no absolute
positions, so a remembered target has to be stored relative to the creature and
updated as it moves. Replay is unaffected as long as the state changes only
inside the decide step.

## Open questions

Whether memory is state on the brain or a component of its own that a sense
fills in, which would keep brains stateless and easier to test.

What a network brain does with it, which ties into the open input question in
[0018](../decisions/0018-brain-computation-model-and-warm-starting.md).
