package io.durbs.places.elasticsearch

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import io.durbs.places.PlaceService
import io.durbs.places.RESTChain
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequestBuilder
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder
import org.elasticsearch.client.Client
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import ratpack.config.ConfigData
import ratpack.server.Service
import ratpack.server.StartEvent
import ratpack.server.StopEvent

@CompileStatic
@Slf4j
class ElasticsearchModule extends AbstractModule {

  @Override
  protected void configure() {

    bind(PlaceService).to(ElasticsearchPlaceService)
    bind(RESTChain)
  }

  @Provides
  @Singleton
  ElasticsearchConfig provideConfig(final ConfigData configData) {

    configData.get(ElasticsearchConfig.CONFIG_ROOT, ElasticsearchConfig)
  }

  @Provides
  @Singleton
  Client provideElasticSearchClient(final ElasticsearchConfig elasticsearchConfig) {

    TransportClient.builder().build()
      .addTransportAddress(
      new InetSocketTransportAddress(InetAddress.getByName(elasticsearchConfig.hostname), elasticsearchConfig.hostport))
  }

  @Provides
  @Singleton
  public Service teardown(final Client elasticsearchClient, final ElasticsearchConfig elasticsearchConfig) {

    new Service() {

      @Override
      public void onStart(final StartEvent event) throws Exception {

        final IndicesExistsRequestBuilder indicesExistsRequestBuilder = elasticsearchClient
          .admin()
          .indices()
          .prepareExists(elasticsearchConfig.index)

        if (!indicesExistsRequestBuilder.get().exists) {

          final CreateIndexRequestBuilder createIndexRequestBuilder = elasticsearchClient
            .admin()
            .indices()
            .prepareCreate(elasticsearchConfig.index)

          log.info("Creating index ${elasticsearchConfig.index}...")
          createIndexRequestBuilder.get()
          log.info("Created index ${elasticsearchConfig.index}")

          final PutMappingRequestBuilder putMappingRequestBuilder = elasticsearchClient
            .admin()
            .indices()
            .preparePutMapping(elasticsearchConfig.index)
            .setType(elasticsearchConfig.type)

          putMappingRequestBuilder.setSource(elasticsearchConfig.geoIndexTypeHintJSON)

          log.info("Setting type for index ${elasticsearchConfig.index}...")
          putMappingRequestBuilder.get()
          log.info("Set type for index ${elasticsearchConfig.index}")
        }
      }

      @Override
      public void onStop(final StopEvent event) throws Exception {

        log.info('Closing elasticsearch client...')
        elasticsearchClient.close()

        log.debug('Elasticsearch client closed.')
      }
    }
  }

}

