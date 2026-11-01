# Data safety form: draft answers

Based on Orlumen's code and on Google's published guidance for the Google Mobile Ads SDK (IP address, user product interactions, diagnostics, and device identifiers, collected and shared for advertising, analytics and fraud prevention). Re-check against Play Console's current form and the SDK page before submitting.

## Data collected and shared by the app itself
None. No account, no analytics SDK, no crash-reporting SDK, no own servers.

## Data on device only (not collected, because it never leaves the device)
- Audio level analysis: processed in memory, never stored or transmitted.
- Now-playing title, artist, album art: read from media controls, kept in memory.
- Foreground app package name: used locally to show or hide the lights.
- Settings, presets, custom palettes, chosen app lists: stored locally.

## Data collected through the Google Mobile Ads SDK (free version)
| Play data type | Collected | Shared | Purpose | Optional |
|---|---|---|---|---|
| Approximate location (from IP address) | Yes | Yes | Advertising or marketing, Analytics, Fraud prevention | Yes |
| App activity: app interactions | Yes | Yes | Advertising or marketing, Analytics, Fraud prevention | Yes |
| App info and performance: diagnostics | Yes | Yes | Analytics, Fraud prevention | Yes |
| Device or other IDs (advertising ID, app set ID) | Yes | Yes | Advertising or marketing, Analytics, Fraud prevention | Yes |

Notes:
- The ads SDK starts only after consent where required and never for Orlumen Pro owners.
- The consent form (Google UMP) runs for users in the EEA, UK and Switzerland.
- Purchases use Google Play Billing. Play processes payment; the app receives only ownership status.

## Security practices
- Data is encrypted in transit (the ads SDK uses TLS).
- Users can ask for data deletion: nothing is held by the developer; ad data is governed by Google and the user's ad settings.
- Does the app follow the Families policy? No. Not designed for children.

## Other declarations to check in Play Console
- Ads: Yes, contains ads.
- Target audience: 13 and over (do not select children).
- Advertising ID declaration: Yes, used for advertising (the manifest declares AD_ID).
