package com.redhat.photogallery.like;

import java.util.List;

import javax.inject.Inject;
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
import com.redhat.photogallery.common.data.LikesMessage;

import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.MessageProducer;

@Path("/likes")
public class LikeResource {

    private static final Logger LOG = LoggerFactory.getLogger(LikeResource.class);

    MessageProducer<JsonObject> topic;


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
            savedItem.persist();
        }
        LOG.info("Updated in data store {}", savedItem);

        LikesMessage likesMessage = createLikesMessage(savedItem);
        topic.write(JsonObject.mapFrom(likesMessage));
        LOG.info("Published {} update on topic {}", likesMessage, topic.address());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response readAllLikes() {
        List<LikesItem> items = LikesItem.listAll();
        LOG.info("Returned all {} items", items.size());
        return Response.ok(new GenericEntity<List<LikesItem>>(items){}).build();
    }

    private LikesMessage createLikesMessage(LikesItem item) {
        LikesMessage msg = new LikesMessage();
        msg.setId(item.id);
        msg.setLikes(item.likes);
        return msg;
    }
}