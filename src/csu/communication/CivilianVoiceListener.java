package csu.communication;

import rescuecore2.standard.messages.AKSpeak;

public interface CivilianVoiceListener {
	void hear(final AKSpeak message);
}
