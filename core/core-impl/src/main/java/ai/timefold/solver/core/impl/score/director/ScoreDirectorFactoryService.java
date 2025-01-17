package ai.timefold.solver.core.impl.score.director;

import java.util.function.Supplier;

import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.score.Score;
import ai.timefold.solver.core.config.score.director.ScoreDirectorFactoryConfig;
import ai.timefold.solver.core.config.solver.EnvironmentMode;
import ai.timefold.solver.core.impl.domain.solution.descriptor.SolutionDescriptor;

/**
 * All {@link ScoreDirectorFactory} implementations must provide an implementation of this interface,
 * as well as an entry in META-INF/services/ai.timefold.solver.core.impl.score.director.ScoreDirectorFactoryService file.
 * This makes it available for discovery in {@link ScoreDirectorFactoryFactory} via {@link java.util.ServiceLoader}.
 *
 * @param <Solution_> the solution type, the class with the {@link PlanningSolution} annotation
 * @param <Score_> the score type to go with the solution
 */
public interface ScoreDirectorFactoryService<Solution_, Score_ extends Score<Score_>> {

    /**
     * If multiple services are available for the same config, the one with the higher priority is picked.
     * Currently unused.
     *
     * @return
     */
    default int getPriority() {
        return Integer.MAX_VALUE;
    }

    /**
     *
     * @return never null, the score director type that is implemented by the factory
     */
    ScoreDirectorType getSupportedScoreDirectorType();

    /**
     * Returns a {@link Supplier} which returns new instance of a score director defined by
     * {@link #getSupportedScoreDirectorType()}.
     * This is done so that the actual factory is only instantiated after all the configuration fail-fasts have been
     * performed.
     *
     * @param classLoader
     * @param solutionDescriptor never null, solution descriptor provided by the solver
     * @param config never null, configuration to use for instantiating the factory
     * @param environmentMode never null
     * @return null when this type is not configured
     * @throws IllegalStateException if the configuration has an issue
     */
    Supplier<AbstractScoreDirectorFactory<Solution_, Score_>> buildScoreDirectorFactory(ClassLoader classLoader,
            SolutionDescriptor<Solution_> solutionDescriptor, ScoreDirectorFactoryConfig config,
            EnvironmentMode environmentMode);

}
