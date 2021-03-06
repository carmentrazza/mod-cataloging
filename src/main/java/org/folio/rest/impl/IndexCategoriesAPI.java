package org.folio.rest.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Handler;
import org.folio.cataloging.business.codetable.ValueLabelElement;
import org.folio.cataloging.log.Log;
import org.folio.rest.jaxrs.model.Category;
import org.folio.rest.jaxrs.model.IndexCategoryCollection;
import org.folio.rest.jaxrs.model.View;
import org.folio.rest.jaxrs.resource.IndexCategoriesResource;

import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.function.Function;
import org.folio.cataloging.log.MessageCatalog;

import static java.util.stream.Collectors.toList;
import static org.folio.cataloging.integration.CatalogingHelper.doGet;

/**
 * Logical views RESTful APIs.
 *
 * @author
 * @since 1.0
 */
public class IndexCategoriesAPI implements IndexCategoriesResource {
    protected final Log logger = new Log(IndexCategoriesAPI.class);

    // This is the adapter that converts existing value objects (logical categories in this case)
    // in OKAPI resources.
    private Function<ValueLabelElement, Category> adapter = source -> {
        final Category logicalCategory = new Category();
        logicalCategory.setCode(source.getValue());
        return logicalCategory;
    };


    @Override
    public void getIndexCategories(Type type, String lang, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
          doGet((storageService, future) -> {
            try {
                final IndexCategoryCollection container = new IndexCategoryCollection();
                container.setCategories(
                        storageService.getIndexCategories(type,lang)
                                .stream()
                                .map(adapter)
                                .collect(toList()));

                return container;
            } catch (final Exception exception) {
                logger.error(MessageCatalog._00010_DATA_ACCESS_FAILURE, exception);
                return null;
            }
        }, asyncResultHandler, okapiHeaders, vertxContext);
    }

    @Override
    public void postIndexCategories(String lang, Category entity, Map<String, String> okapiHeaders, Handler<AsyncResult<Response>> asyncResultHandler, Context vertxContext) throws Exception {
      throw new IllegalArgumentException();
    }
}
