package net.fabricmc.loom.task;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.providers.LaunchProvider;
import net.fabricmc.loom.util.Constants;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class UnpickJarTask extends JavaExec {
	File inputJar;
	File unpickDefinition;

	File outputJar;

	public UnpickJarTask() {
		getOutputs().upToDateWhen(e -> false);
		classpath(getProject().getConfigurations().getByName(Constants.Configurations.UNPICK_CLASSPATH));
		setMain("daomephsta.unpick.cli.Main");
	}

	@Override
	public void exec() {
		fileArg(getInputJar(), getOutputJar(), getUnpickDefinition());
		fileArg(getConstantJar());

		// Classpath
		fileArg(getExtension().getMinecraftMappedProvider().getMappedJar());
		fileArg(getMinecraftDependencies());

		writeUnpickLogConfig();
		systemProperty("java.util.logging.config.file", getExtension().getUnpickLoggingConfigFile().getAbsolutePath());

		super.exec();
	}

	private void writeUnpickLogConfig() {
		try (InputStream is = LaunchProvider.class.getClassLoader().getResourceAsStream("unpick-logging.properties")) {
			Files.deleteIfExists(getExtension().getUnpickLoggingConfigFile().toPath());
			Files.copy(is, getExtension().getUnpickLoggingConfigFile().toPath());
		} catch (IOException e) {
			throw new RuntimeException("Failed to copy unpick logging config", e);
		}
	}

	private File[] getMinecraftDependencies() {
		return getProject().getConfigurations().getByName(Constants.Configurations.MINECRAFT_DEPENDENCIES)
				.resolve().toArray(new File[0]);
	}

	private File getConstantJar() {
		return getProject().getConfigurations().getByName(Constants.Configurations.MAPPING_CONSTANTS).getSingleFile();
	}

	@InputFile
	public File getInputJar() {
		return inputJar;
	}

	public UnpickJarTask setInputJar(File inputJar) {
		this.inputJar = inputJar;
		return this;
	}

	@InputFile
	public File getUnpickDefinition() {
		return unpickDefinition;
	}

	public UnpickJarTask setUnpickDefinition(File unpickDefinition) {
		this.unpickDefinition = unpickDefinition;
		return this;
	}

	@OutputFile
	public File getOutputJar() {
		return outputJar;
	}

	public UnpickJarTask setOutputJar(File outputJar) {
		this.outputJar = outputJar;
		return this;
	}

	private void fileArg(File... files) {
		for (File file : files) {
			args(file.getAbsolutePath());
		}
	}

	@Internal
	protected LoomGradleExtension getExtension() {
		return getProject().getExtensions().getByType(LoomGradleExtension.class);
	}
}
