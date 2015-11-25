/*
 *
 * SchemaCrawler
 * http://www.schemacrawler.com
 * Copyright (c) 2000-2015, Sualeh Fatehi.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 */
package schemacrawler.utility;


import static java.util.Objects.requireNonNull;
import static sf.util.Utility.containsWhitespace;
import static sf.util.Utility.isBlank;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Identifiers
{

  private static final Logger LOGGER = Logger
    .getLogger(Identifiers.class.getName());

  private static final Predicate<String> filter = word -> !isBlank(word);
  private static final Function<String, String> map = word -> word.trim()
    .toUpperCase();

  private static final Pattern isIdentifierPattern = Pattern
    .compile("^[\\p{Nd}\\p{L}\\p{M}_]*$");
  private static final Pattern isNumericPattern = Pattern.compile("^\\p{Nd}*$");

  /**
   * Checks if the text is composed of all numbers.
   *
   * @param text
   *        Text to check.
   * @return Whether the string consists of all numbers.
   */
  private static boolean isIdentifier(final String text)
  {
    return text == null || text.isEmpty()
           || isIdentifierPattern.matcher(text).matches();
  }

  /**
   * Checks if the text is composed of all numbers.
   *
   * @param text
   *        Text to check.
   * @return Whether the string consists of all numbers.
   */
  private static boolean isNumeric(final String text)
  {
    return text == null || text.isEmpty()
           || isNumericPattern.matcher(text).matches();
  }

  private static Collection<String> loadSql2003ReservedWords()
  {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(Identifiers.class
      .getResourceAsStream("/sql2003_reserved_words.txt")));

    final Set<String> reservedWords = new HashSet<>();
    reader.lines().filter(filter).map(map)
      .forEach(reservedWord -> reservedWords.add(reservedWord));
    return reservedWords;
  }

  private static Collection<String> lookupReservedWords(final DatabaseMetaData metaData)
  {
    String sqlKeywords = "";
    try
    {
      sqlKeywords = metaData.getSQLKeywords();
    }
    catch (final Exception e)
    {
      LOGGER.log(Level.WARNING, "Could not retrieve SQL keywords metadata", e);
    }

    return Stream.of(sqlKeywords.split(",")).filter(filter).map(map)
      .collect(Collectors.toSet());
  }

  private final String identifierQuoteString;
  private final Collection<String> reservedWords;

  public Identifiers()
  {
    reservedWords = loadSql2003ReservedWords();

    identifierQuoteString = "";
  }

  public Identifiers(final Connection connection)
    throws SQLException
  {
    this(connection, null);
  }

  public Identifiers(final Connection connection,
                     final String identifierQuoteString)
                       throws SQLException
  {
    requireNonNull(connection, "No connection provided");

    reservedWords = loadSql2003ReservedWords();
    reservedWords.addAll(lookupReservedWords(connection.getMetaData()));

    if (identifierQuoteString == null)
    {
      final String metaDataIdentifierQuoteString = connection.getMetaData()
        .getIdentifierQuoteString();
      if (metaDataIdentifierQuoteString == null)
      {
        this.identifierQuoteString = "";
      }
      else
      {
        this.identifierQuoteString = metaDataIdentifierQuoteString;
      }
    }
    else
    {
      this.identifierQuoteString = identifierQuoteString;
    }
  }

  public String getIdentifierQuoteString()
  {
    return identifierQuoteString;
  }

  public Collection<String> getReservedWords()
  {
    return new HashSet<>(reservedWords);
  }

  public boolean isQuotedName(final String name)
  {
    if (isBlank(name))
    {
      return false;
    }

    final int quoteLength = identifierQuoteString.length();
    return name.startsWith(identifierQuoteString)
           && name.endsWith(identifierQuoteString)
           && name.length() >= quoteLength * 2;
  }

  public boolean isReservedWord(final String word)
  {
    return filter.test(word) && reservedWords.contains(map.apply(word));
  }

  public boolean isToBeQuoted(final String name)
  {
    if (name == null || name.isEmpty())
    {
      return false;
    }
    else
    {
      return containsWhitespace(name) || isNumeric(name)
             || containsSpecialCharacters(name) || isReservedWord(name);
    }
  }

  public String quotedName(final String name)
  {
    final String quotedName;
    if (isToBeQuoted(name))
    {
      quotedName = identifierQuoteString + name + identifierQuoteString;
    }
    else
    {
      quotedName = name;
    }
    return quotedName;
  }

  public String unquotedName(final String name)
  {
    if (isBlank(name))
    {
      return name;
    }

    final String unquotedName;
    final int quoteLength = identifierQuoteString.length();
    if (isQuotedName(name))
    {
      unquotedName = name.substring(quoteLength, name.length() - quoteLength);
    }
    else
    {
      unquotedName = name;
    }
    return unquotedName;
  }

  private boolean containsSpecialCharacters(final String name)
  {
    return !isIdentifier(name);
  }

}