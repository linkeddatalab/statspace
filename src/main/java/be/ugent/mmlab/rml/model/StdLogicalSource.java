package be.ugent.mmlab.rml.model;

import be.ugent.mmlab.rml.vocabulary.Vocab.QLTerm;

/**
 *  Concrete implementation of a Logical Source
 * 
 * @author mielvandersande, andimou
 */
public class StdLogicalSource implements LogicalSource {

    private String iterator;
    private QLTerm referenceFormulation = QLTerm.SQL_CLASS;
    private String source;

    public StdLogicalSource(String iterator, String source) {
        this.iterator = iterator;
        this.source = source;
    }

    public StdLogicalSource(String iterator) {
        this.iterator = iterator;
    }

    public StdLogicalSource(String iterator, String source, QLTerm queryLanguage) {
        this.iterator = iterator;
        this.source = source;
        this.referenceFormulation = queryLanguage;
    }
    
    @Override
    public String getIterator() {
        return iterator;
    }

    @Override
    public QLTerm getReferenceFormulation() {
        return referenceFormulation;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "[StdLogicalSource : iterator = " + iterator
                + "; source = " + source + "; referenceFormulation = " + referenceFormulation + "]";
    }
}
