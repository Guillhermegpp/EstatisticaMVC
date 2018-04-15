package br.com.estatistica.estatistica.view;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

import br.com.estatistica.estatistica.controller.ExerciseController;
import br.com.estatistica.estatistica.controller.ExerciseControllerMedia;
import br.com.estatistica.estatistica.controller.ExerciseControllerMediana;
import br.com.estatistica.estatistica.controller.ExerciseControllerModa;
import br.com.estatistica.estatistica.model.Model;

public class View implements Observer {

	TelegramBot bot = TelegramBotAdapter.build("560936083:AAFxIv0Rq1tO7SqILKU2hBirWeo1e_3mfG8");

	// Object that receives messages
	GetUpdatesResponse updatesResponse;
	// Object that send responses
	SendResponse sendResponse;
	// Object that manage chat actions like "typing action"
	BaseResponse baseResponse;

	int queuesIndex = 0;
	boolean searchBehaviour = false;
	Properties properties;

	ExerciseController exerciseController; // Strategy Pattern -- connection View -> Controller
	private Model model;

	public View(Model model) {
		this.model = model;
	}

	public void setControllerSearch(ExerciseController exerciseController) { // Strategy Pattern
		this.exerciseController = exerciseController;
	}

	public void receiveUsersMessages() {

		// infinity loop
		while (true) {

			// taking the Queue of Messages
			updatesResponse = bot.execute(new GetUpdates().limit(100).offset(queuesIndex));

			// Queue of messages
			List<Update> updates = updatesResponse.updates();

			// taking each message in the Queue
			for (Update update : updates) {
				executa(update);
			}
		}
	}

	public void executa(Update update) {
		// updating queue's index
		queuesIndex = update.updateId() + 1;

		if (this.searchBehaviour == true) {
			this.callController(update);

		} else if (update.message().text().equals("Media")) {
			setControllerSearch(new ExerciseControllerMedia(model, this));
			sendResponse = bot.execute(new SendMessage(update.message().chat().id(),
					"Digites os valores de entrada separados por ponto e virgula (;)!"));
			this.searchBehaviour = true;

		} else if (update.message().text().equals("Mediana")) {
			setControllerSearch(new ExerciseControllerMediana(model, this));
			sendResponse = bot.execute(new SendMessage(update.message().chat().id(),
					"Digites os valores de entrada separados por ponto e virgula (;)!"));
			this.searchBehaviour = true;

		} else if (update.message().text().equals("Moda")) {
			setControllerSearch(new ExerciseControllerModa(model, this));
			sendResponse = bot.execute(new SendMessage(update.message().chat().id(),
					"Digites os valores de entrada separados por ponto e virgula (;)!"));
			this.searchBehaviour = true;

		} else if(update.message().text().equals("/start")) {
			Keyboard replyKeyboardMarkup = new ReplyKeyboardMarkup(new String[] { "Media", "Moda", "Mediana" })
					.oneTimeKeyboard(false) // optional
					.resizeKeyboard(true) // optional
					.selective(true);
			bot.execute(new SendMessage(update.message().chat().id(), "O que deseja calcular?")
					.replyMarkup(replyKeyboardMarkup));
		}
	}

	public void callController(Update update) {
		this.exerciseController.calcular(update);
	}

	public void update(long chatId, String studentsData) {
		sendResponse = bot.execute(new SendMessage(chatId, studentsData));
		this.searchBehaviour = false;
	}

	public void sendTypingMessage(Update update) {
		baseResponse = bot.execute(new SendChatAction(update.message().chat().id(), ChatAction.typing.name()));
	}

}
