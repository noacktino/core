package de.btu.openinfra.plugins.solr;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

import de.btu.openinfra.backend.exception.OpenInfraExceptionTypes;
import de.btu.openinfra.plugins.PluginProperties;
import de.btu.openinfra.plugins.solr.db.pojos.SolrComplexQueryPartPojo;
import de.btu.openinfra.plugins.solr.db.pojos.SolrSearchPojo;
import de.btu.openinfra.plugins.solr.exception.OpenInfraSolrException;

public class SolrQueryParser {

    private Locale locale;

    /**
     * This method returns a search query in Solr syntax. It will parse the
     * requested SolrSearchPojo and replace the field names for correct matching
     * to the Solr index.
     *
     * @return A String that represents the search query in Solr syntax.
     */
    public String getSolrSyntaxQuery(SolrSearchPojo pojo, Locale locale) {
        if (pojo != null) {
            this.locale = locale;
            // Determine which query is set.
            if (pojo.getRawSolrQuery() != null && !pojo.getRawSolrQuery()
                    .equals("")) {
                // start the parse process for a simple Solr query
                return parseRawSolrQuery(pojo.getRawSolrQuery());
            } else if (pojo.getComplexQueryPart().size() > 0) {
                // start the parse process for a complex Solr query
                return parseComplexSolrQuery(pojo.getComplexQueryPart());
            }
        }

        // TODO replace with SolrExceptionType
        throw new OpenInfraSolrException(
                OpenInfraExceptionTypes.INTERNAL_SERVER_EXCEPTION);
    }

    /**
     * This method parses a raw Solr query. It only converts the field
     * identifier.
     *
     * @param rawQuery
     * @return
     */
    private String parseRawSolrQuery(String rawQuery) {
        // TODO handle "x y" queries

        // pattern to capture everything in front of a colon
        Pattern pattern = Pattern.compile("(.*):");
        Matcher matcher = null;
        String attributeTypeName = "";

        // split on every whitespace to separate every part of the query
        for (String part : rawQuery.split(" ")) {
            // retrieve the attribute type

            matcher = pattern.matcher(part);
            if (matcher.find()) {
                // add the name to the result list
                for (int i = 0; i < matcher.groupCount(); i++) {
                    attributeTypeName = matcher.group(i);
                    // remove the colon
                    attributeTypeName = attributeTypeName
                            .substring(0, attributeTypeName.length()-1);
                    // convert the type name and replace it in the raw query
                    rawQuery = rawQuery.replaceAll(
                            attributeTypeName,
                            SolrCharacterConverter.convert(attributeTypeName));
                }

            }
        }
        return rawQuery;
    }

    /**
     * This method parses a complex Solr query. The locale must not be null
     * because it is necessary to parse the query parts.
     *
     * @param complexQueryList
     * @return
     */
    private String parseComplexSolrQuery(
            List<SolrComplexQueryPartPojo> complexQueryList) {
        if (locale != null) {
            String query = "";
            boolean firstRun = true;
            for (SolrComplexQueryPartPojo part : complexQueryList) {
                query += " " + paresePart(part, firstRun);
                firstRun = false;
            }
            // remove leading and trailing whitespaces
            return query.trim();
        }

        // TODO replace with SolrExceptionType
        throw new OpenInfraSolrException(
                OpenInfraExceptionTypes.INTERNAL_SERVER_EXCEPTION);
    }

    /**
     * This method parses a SolrComplexQueryPartPojo to Solr syntax. There are
     * some special definitions for the POJO properties.
     *
     * - attribute value is mandatory
     * - till attribute value is only mandatory if relational operator is BETWEEN
     * - relational operator is mandatory if attribute type is set, default: EQUAL
     * - logic operator is optional, default: OR
     * - relevance is optional
     * @param part
     * @param firstRun
     * @return
     */
    private String paresePart(SolrComplexQueryPartPojo part, boolean firstRun) {
        String query = "";
        // add the attribute type
        if (part.getAttributeType() != null) {
            query += SolrCharacterConverter.convert(part.getAttributeType());
        }

        // add the delimiter between attribute type and attribute value
        if (!query.equals("")) {
            query += ":";
        }

        // add the mandatory identifier
        if (part.getMandatory() != null) {
            query = part.getMandatory().getString() + query;
        }

        // add the attribute value in different ways depending on the relational
        // operator
        if (part.getAttributeValue() != null) {
            if (part.getRelationalOperator() != null) {
                switch (part.getRelationalOperator()) {
                case GREATER_THAN:
                case SMALLER_THAN:
                    // only possible for numeric values
                    if (NumberUtils.isNumber(part.getAttributeValue())) {
                        query += String.format(
                                part.getRelationalOperator().getString(),
                                part.getAttributeValue());
                    } else {
                        // TODO replace with SolrExceptionType
                        throw new OpenInfraSolrException(
                                OpenInfraExceptionTypes
                                .INTERNAL_SERVER_EXCEPTION);
                    }
                    break;
                case BETWEEN:
                    query += String.format(
                            part.getRelationalOperator().getString(),
                            part.getAttributeValue(),
                            part.getTillAttributeValue());
                    break;
                default:
                    query += maskString(part.getAttributeValue());
                    break;
                }
            } else {
                query += part.getAttributeValue();
            }
        } else {
            // return an empty string if no attribute value is passed
            return "";
        }

        // only add the logic operator if it is not the first part we match
        if (!firstRun) {
            if (part.getLogicOperator() != null) {
                query = part.getLogicOperator().getString() + " " + query;
            }
        }

        // add fuzziness if required
        if (part.isFuzziness()) {
            query += "~" + PluginProperties.getProperty(
                    SolrPropertyKeys.SOLR_DEFUALT_FUZZY.getKey(), "Solr");
        }

        // add the relevance if it was passed
        if (part.getRelevance() != 0) {
            query += "^" + part.getRelevance();
        }

        return query;
    }

    /**
     * This method checks if a string contains whitespaces. If so a the string
     * will be surrounded by quotation marks.
     *
     * @param str The input string that should be checked.
     * @return    The modified string.
     */
    private String maskString(String str) {
        if (str.contains(" ")) {
            // we must be sure that the client does not added quotation marks
            // as well
            if (!str.startsWith(("\"")) && !str.endsWith(("\""))) {
                str = "\"" + str +"\"";
            };
        }
        return str;
    }

}
