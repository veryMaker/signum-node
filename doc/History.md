[History of Burst](https://burstwiki.org/en/history-of-burst/)

```
2020-05-10 v2.5.0 (Backport of fixes from v3.0.0 and many other improvements)
- New deadline computation formula aiming at more stable block times [CIP24](https://github.com/burst-apps-team/CIPs/blob/master/cip-0024.md)
- More powerful and cheaper running smart contracts [CIP20](https://github.com/burst-apps-team/CIPs/blob/master/cip-0020.md)
- Enforce slot fees [CIP23](https://github.com/burst-apps-team/CIPs/blob/master/cip-0023.md)
- Can run on recent Java versions (previously limited to Java 8)
- A new GUI implementation
- Improved CORS support
- Fixed Escrow ID in Escrow Result Attachment Protobuf is always 2
- Fixed Get Peer Handler in gRPC API is never initialized
- Fixed GetOrdersHandler always returns ask orders
- Fixed Bugs in AT API Implementation

2019-08-13 v2.4.2
- Fixed HTTP API Encoding being reported to the client incorrectly, leading to the client incorrectly parsing special characters
- Limit maximum number of items returned by HTTP API
- Fixed NPE in ProtoBuilder when fetching an account

2019-07-15 v2.4.1
- Default to submit nonce whitelist off
- Revert removal of rejection of surplus parameters
- Add option to bind V2 API to specific interface
- MariaDB Settings tweaks 
- Various bug fixes and improvements

2019-07-01 v2.4.0
- Massive DB optimization, much much faster sync speed (Benchmarked at 7 hours to sync to block 600k on a 4C/8T 16GB RAM system, under MariaDB 10.3)
- Implemented CIP19 - View incoming & outgoing multi-out transactions in the UI
- Added new feature to sign arbitrary messages using UI
- Fixed gRPC error descriptions
- Comprehensive V2 API with all functionality of V1 implemented
- Auto pop-off on block push fail with slow back-off, should prevent nodes from getting stuck forever
- UTStore should produce waaaay less spam
- CORS on by default
- Minimum previous version is now v2.3.0
- Enforce fee structure (Inactive)
- Improved algorithm for transaction candidate selection
- Check in gRPC generated files (simplifies build)
- Tighter timings for sync threads
- Burstkit4j integration
- Rewrite support for UI (Apps that utilize deep linking such as phoenix can now be hosted by BRS)
- Add a method to not submit passphrase when solo mining by configuring passphrase in config and only submitting account ID, and an option to disallow others from mining on your node
- AT debug option
- Improvements to AT implementation
- Web UI: Display AT messages as both string and hex
- Fix UT Store failed removal
- Re-add `getGuaranteedBalance` HTTP API call as lots of clients depended on it
- Test endpoint support for QR code generator
- Implemented CIP20 (Inactive)

2018-04-04 v2.3.0
           Fix of major security vulnerability where passphrase was sent to node upon login
           gRPC-based V2 API. Currently only contains calls needed for mining, will be expanded in future if well received.
           Migrate to GSON as JSON library
           Significantly improve sync speed, as well as other minor performance improvements
           New Semver-based versioning system
           Fix bug where reward recipient assignments would not go into unconfirmed transactions
           Lightweight Desktop GUI, with tray icon (For windows and mac, can be disabled with "--headless" command line argument)
           Automatically add conf/ directory to classpath
           Configurable TestNet UI/API port
           New getAccountsWithName API call
           UI: Fix 24h timestamp display option
           Allow development versions of wallet to run on TestNet only
           Fixed bug where string validation could fail in certain locales
           Use FlywayDB for database migration management

2018-05-30 2.2.0
           "Pre-Dymaxion" HF1 release (Burst hard fork/upgrade)
           @500k: 4x bigger blocks, multi-out transactions, dynamic fees
           @502k: PoC2

2018-03-15 2.0.0
           BRS - Burst Reference Software:
           Burst namespace, some NXT legacy is in API data sent P2P
           streamlined configuration namespace, more logical and intuitive
           migrated to JOOQ, supports many  DB backends; only H2 and mariaDB
           in-code to prevent bloat, all others via DB-manager
           UPnP functionality to help with router configuration for public nodes
           removed lots of unused code, updated many UI libraries
           significant improvements in P2P handling: re-sync speed, fork-handling
           peer acquisition
           Squashed many bugs and vulnerabilities, using subresource integrity
           test coverage went from 0% to over 20%

2017-10-28 1.3.6cg
           multi-DB support: added Firebird, re-added H2; support for quick
           binary dump and load

2017-09-04 1.3.4cg
           improved database deployment; bugfix: utf8 encoding

2017-08-11 1.3.2cg
           1st official PoCC release: MariaDB backend based on 1.2.9
```

[Versions up to 2.2.7](https://github.com/poc-consortium/burstcoin/releases)

[Versions up to 1.2.9](https://github.com/burst-team/burstcoin/releases)
