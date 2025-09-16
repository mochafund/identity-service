# Kafka Integration TODO

## Goal
Implement complete Kafka publish-consume flow within identity-service for WorkspaceMembershipEvent.

## Tasks

### 1. Configure Kafka properties in application.yml
- [ ] Add Kafka bootstrap servers configuration
- [ ] Configure topics (workspace-membership-events)
- [ ] Set up serialization/deserialization properties

### 2. Complete KafkaProducer.send() implementation in MembershipService.deleteMembership()
- [ ] Remove TODO comment at line 86
- [ ] Actually send the created WorkspaceMembershipEvent to Kafka
- [ ] Specify correct topic name

### 3. Create KafkaConsumer service to consume WorkspaceMembershipEvent messages
- [ ] Create new KafkaConsumer service class
- [ ] Set up consumer group configuration
- [ ] Add dependency injection for the consumer

### 4. Implement event listener method to log consumed WorkspaceMembershipEvent types
- [ ] Create @KafkaListener method
- [ ] Log the event type when WorkspaceMembershipEvent is consumed
- [ ] Handle deserialization of the event

### 5. Add Kafka configuration class for topics and serializers/deserializers
- [ ] Create KafkaConfig configuration class
- [ ] Define topic beans
- [ ] Configure JSON serializers/deserializers for WorkspaceMembershipEvent

### 6. Test the complete publish-consume flow within identity-service
- [ ] Test membership deletion triggers event publishing
- [ ] Verify consumer receives and logs the event
- [ ] Confirm the full cycle works end-to-end

## Notes
- This establishes the basic Kafka infrastructure within identity-service
- Future expansion will include cross-service communication
- Event data processing will be implemented later - focus on infrastructure first
- **Environment Configuration**: Add all Kafka-related environment variables to `.env` file and configure them in the `docker-compose.yml` environment section