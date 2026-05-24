package ru.redstonemaster.web.service;

import org.springframework.stereotype.Service;
import ru.redstonemaster.web.model.ModInfo;

@Service
public class ModInfoService {

	private static final ModInfo INFO = new ModInfo(
			"Redstone Master",
			"1.0.0",
			"1.21.11",
			"Клиентский Fabric-мод с обучающими материалами по редстоун-механикам Minecraft.",
			"https://github.com/SvoNaR/redstone-master"
	);

	public ModInfo getInfo() {
		return INFO;
	}
}
