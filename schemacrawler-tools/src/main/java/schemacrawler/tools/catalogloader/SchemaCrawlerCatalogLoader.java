/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.tools.catalogloader;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;

import schemacrawler.crawl.SchemaCrawler;
import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.tools.options.Config;

public class SchemaCrawlerCatalogLoader implements CatalogLoader {

  private final String databaseSystemIdentifier;
  private SchemaRetrievalOptions schemaRetrievalOptions;
  private SchemaCrawlerOptions schemaCrawlerOptions;
  private Config additionalConfiguration;
  private Connection connection;

  public SchemaCrawlerCatalogLoader() {
    databaseSystemIdentifier = null;
  }

  protected SchemaCrawlerCatalogLoader(final String databaseSystemIdentifier) {
    this.databaseSystemIdentifier =
        requireNonNull(databaseSystemIdentifier, "No database system identifier provided");
  }

  @Override
  public Config getAdditionalConfiguration() {
    if (additionalConfiguration == null) {
      return new Config();
    } else {
      return additionalConfiguration;
    }
  }

  @Override
  public Connection getConnection() {
    return connection;
  }

  @Override
  public String getDatabaseSystemIdentifier() {
    return databaseSystemIdentifier;
  }

  @Override
  public SchemaCrawlerOptions getSchemaCrawlerOptions() {
    if (schemaCrawlerOptions == null) {
      return SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    } else {
      return schemaCrawlerOptions;
    }
  }

  @Override
  public SchemaRetrievalOptions getSchemaRetrievalOptions() {
    if (schemaRetrievalOptions == null) {
      return SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();
    } else {
      return schemaRetrievalOptions;
    }
  }

  @Override
  public Catalog loadCatalog() throws Exception {
    requireNonNull(connection, "No connection provided");
    requireNonNull(schemaRetrievalOptions, "No database specific overrides provided");

    final SchemaCrawler schemaCrawler =
        new SchemaCrawler(connection, schemaRetrievalOptions, schemaCrawlerOptions);
    final Catalog catalog = schemaCrawler.crawl();

    return catalog;
  }

  @Override
  public void setAdditionalConfiguration(final Config additionalConfiguration) {
    this.additionalConfiguration = additionalConfiguration;
  }

  @Override
  public void setConnection(final Connection connection) {
    this.connection = connection;
  }

  @Override
  public void setSchemaCrawlerOptions(final SchemaCrawlerOptions schemaCrawlerOptions) {
    this.schemaCrawlerOptions = schemaCrawlerOptions;
  }

  @Override
  public void setSchemaRetrievalOptions(final SchemaRetrievalOptions schemaRetrievalOptions) {
    this.schemaRetrievalOptions = schemaRetrievalOptions;
  }
}
