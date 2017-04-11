package edu.sfsu.worldofbalance.atnsimulator;

import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

import java.io.File;

/**
 * Writes data from a completed simulation to an HDF5 output file.
 * All files will be stored in the output directory supplied to the constructor.
 */
public class OutputFileWriter {

    private File outputDirectory;

    public OutputFileWriter(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    /**
     * Write the given data to an output HDF5 file in `outputFileDirectory`.
     * The filename depends on data.simulationId.
     * @param data the data from a completed simulation
     */
    public void write(OutputFileData data) {
        File outputFile = getOutputFile(data.simulationId);
        IHDF5Writer writer = HDF5Factory.configure(outputFile).writer();

        // Simulation parameters
        writer.writeDouble("/parameters/simulation/step_size",
                data.simulationResults.simulationParameters.stepSize);
        writer.writeBoolean("/parameters/simulation/stop_on_steady_state",
                data.simulationResults.simulationParameters.stopOnSteadyState);

        // System parameters
        ModelParameters p = data.simulationResults.modelParameters;
        writer.writeBoolean("/parameters/system/use_system_carrying_capacity", p.useSystemCarryingCapacity);
        writer.writeDouble("/parameters/system/system_carrying_capacity", p.systemCarryingCapacity);

        // Node parameters
        writer.writeDoubleArray("/parameters/node/metabolic_rate", p.metabolicRate);
        writer.writeDoubleArray("/parameters/node/growth_rate", p.growthRate);
        writer.writeDoubleArray("/parameters/node/carrying_capacity", p.carryingCapacity);

        // Link parameters
        writer.writeDoubleMatrix("/parameters/link/maximum_ingestion_rate", p.maximumIngestionRate);
        writer.writeDoubleMatrix("/parameters/link/predator_interference", p.predatorInterference);
        writer.writeDoubleMatrix("/parameters/link/functional_response_control", p.functionalResponseControl);
        writer.writeDoubleMatrix("/parameters/link/relative_half_saturation_density", p.relativeHalfSaturationDensity);
        writer.writeDoubleMatrix("/parameters/link/half_saturation_density", p.halfSaturationDensity);
        writer.writeDoubleMatrix("/parameters/link/assimilation_efficiency", p.assimilationEfficiency);

        // Biomass data (32-bit floats to save space)
        double[][] doubleBiomass = data.simulationResults.biomass;
        int nodeCount = doubleBiomass[0].length;
        int timesteps = data.simulationResults.timestepsSimulated;
        float[][] floatBiomass = new float[timesteps][nodeCount];
        for (int t = 0; t < timesteps; t++) {
            for (int i = 0; i < nodeCount; i++) {
                floatBiomass[t][i] = (float) doubleBiomass[t][i];
            }
        }
        writer.writeFloatMatrix("/biomass", floatBiomass);

        writer.writeIntArray("/extinction_timesteps", data.simulationResults.extinctionTimesteps);
        writer.writeString("/stop_event", data.simulationResults.stopEvent.toString());
        writer.writeString("/node_config", data.nodeConfig);
        writer.writeDouble("/node_config_biomass_scale", data.nodeConfigBiomassScale);
        writer.writeIntArray("/node_ids", data.originalNodeIds);
        writer.writeString("/food_web_json", data.originalSubweb.toJson());

        writer.close();
    }

    private File getOutputFile(int simulationId) {
        String filename = simulationId == 0 ? "ATN.h5" : "ATN_" + simulationId + ".h5";
        return new File(outputDirectory, filename);
    }
}
