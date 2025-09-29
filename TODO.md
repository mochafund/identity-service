# Identity Service TODO

- [ ] Introduce generic event envelope for published events (type/version/payload).
- [ ] Update Kafka consumer configuration to remove `spring.json.type.mapping` reliance.
- [ ] Ensure workspace events published here use shared type constants and payload schemas.
- [ ] Add contract tests to verify envelope compatibility with workspace-service.
- [ ] Document the workspace and membership event schemas.
