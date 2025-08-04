<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Experience Bar Fix Mod - Copilot Instructions

This is a Minecraft Forge mod project for fixing the experience bar disappearing issue after teleportation in Minecraft 1.20.1.

## Project Structure
- This is a Minecraft Forge mod targeting MC 1.20.1 with Forge 47.2.0+
- Uses Java 17 and Gradle build system
- Main package: `com.github.experiencebarfix`

## Code Style Guidelines
- Follow Java naming conventions
- Use meaningful variable and method names
- Add comprehensive JavaDoc comments for public methods
- Handle exceptions gracefully with proper logging
- Use ForgeConfigSpec for configuration management

## Key Components
- `ExperienceBarFixMod`: Main mod class with initialization
- `ExperienceBarFixer`: Core event handler implementing the robust fix solution
- `Config`: Configuration management using ForgeConfigSpec

## Event Handling
- Use `@SubscribeEvent` for Forge event handling
- Handle teleport commands, dimension changes, and respawn events
- Implement thread-safe collections for tracking pending fixes
- Use server scheduler for delayed execution

## Logging
- Use the mod's logger: `ExperienceBarFixMod.LOGGER`
- Implement debug logging that can be toggled via config
- Log errors appropriately without spamming the console

## Best Practices
- Always check if players are still online before applying fixes
- Use defensive programming with null checks
- Implement fallback methods for robustness
- Clean up resources (remove from tracking sets) when players disconnect
