package com.redhat.photogallery.like;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
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
import com.redhat.photogallery.common.data.LikesAddedMessage;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.MessageProducer;

@Path("/likes")
public class LikeService {

    private static final Logger LOG = LoggerFactory.getLogger(LikeService.class);

    private MessageProducer<JsonObject> topic;

    @Inject
    public void injectEventBus(EventBus eventBus) {
        topic = eventBus.<JsonObject>publisher(Constants.LIKES_TOPIC_NAME);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public void addLikes(LikesItem item) {
        LikesItem savedItem = LikesItem.findById(item.id);
        if (savedItem == null) {
            item.persist();
            savedItem = item;
        }
        else {
            int likes = savedItem.likes + item.likes;
            savedItem.likes = likes;
        }
        LOG.info("Updated in data store {}", savedItem);

        LikesAddedMessage message = createLikesAddedMessage(savedItem);
        topic.write(JsonObject.mapFrom(message));
        LOG.info("Published {} update on topic {}", message, topic.address());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readAllLikes() {
        List<LikesItem> items = LikesItem.listAll();
        LOG.info("Returned all {} items", items.size());
        return Response.ok(new GenericEntity<List<LikesItem>>(items){}).build();
    }

    private LikesAddedMessage createLikesAddedMessage(LikesItem item) {
        LikesAddedMessage msg = new LikesAddedMessage();
        msg.setId(item.id);
        msg.setLikes(item.likes);
        return msg;
    }
}