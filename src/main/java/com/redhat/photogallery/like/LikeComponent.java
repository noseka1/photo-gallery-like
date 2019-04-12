package com.redhat.photogallery.like;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.redhat.photogallery.common.Constants;
import com.redhat.photogallery.common.data.DataStore;
import com.redhat.photogallery.common.data.LikesItem;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.MessageProducer;

@Path("/likes")
public class LikeComponent {

    private static final Logger LOG = LoggerFactory.getLogger(LikeComponent.class);

    private DataStore<LikesItem> dataStore = new DataStore<>();

    MessageProducer<JsonObject> topic;


    @Inject
    public void injectEventBus(EventBus eventBus) {
        topic = eventBus.<JsonObject>publisher(Constants.LIKES_TOPIC_NAME);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void addLikes(LikesItem item) {
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
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readAllLikes() {
        List<LikesItem> items = (dataStore.getAllItems());
        LOG.info("Returned all {} items", dataStore.getAllItems().size());
        return Response.ok(new GenericEntity<List<LikesItem>>(items){}).build();
    }

}