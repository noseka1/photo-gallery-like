package com.redhat.photogallery.like;

import com.redhat.photogallery.common.Constants;
import com.redhat.photogallery.common.ServerComponent;
import com.redhat.photogallery.common.data.DataStore;
import com.redhat.photogallery.common.data.LikesItem;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.MessageProducer;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

public class LikeComponent implements ServerComponent {

	private static final Logger LOG = LoggerFactory.getLogger(LikeComponent.class);

	private DataStore<LikesItem> dataStore = new DataStore<>();

	MessageProducer<JsonObject> topic;

	@Override
	public void registerRoutes(Router router) {
		router.post("/likes").handler(BodyHandler.create()).handler(this::addLikes);
		router.get("/likes").handler(this::readAllLikes);
	}

	@Override
	public void injectEventBus(EventBus eventBus) {
		topic = eventBus.<JsonObject>publisher(Constants.LIKES_TOPIC_NAME);
	}

	private void addLikes(RoutingContext rc) {
		LikesItem item;
		try {
			item = rc.getBodyAsJson().mapTo(LikesItem.class);
		} catch (Exception e) {
			LOG.error("Failed parse item {}", rc.getBodyAsString(), e);
			rc.response().setStatusCode(400).end();
			return;
		}

		LikesItem savedItem = dataStore.getItem(item.getId());
		if (savedItem == null) {
			dataStore.putItem(item);
			savedItem = item;
		}
		else {
			int likes = savedItem.getLikes() + item.getLikes();
			savedItem.setLikes(likes);
			dataStore.putItem(savedItem);
		}
		LOG.info("Updated in data store {}", savedItem);

		topic.write(JsonObject.mapFrom(item));
		LOG.info("Published {} update on topic {}", item, topic.address());

		rc.response().end();
	}

	private void readAllLikes(RoutingContext rc) {
		HttpServerResponse response = rc.response();
		response.putHeader("content-type", "application/json");
		response.end(Json.encodePrettily(dataStore.getAllItems()));
		LOG.info("Returned all {} items", dataStore.getAllItems().size());
	}

}