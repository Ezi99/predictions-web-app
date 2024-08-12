package dto.definition.action;


public class ActionInfoDTO {
    private String type;
    private String property;
    private String expression;
    private String mainEntity;
    private String secondaryEntity;
    private String arg1;
    private String arg2;
    private String calcType;
    private String conditionProperty;
    private String value;
    private String conditionOperator;
    private int conditionsCount;
    private int thenActionCount;
    private int elseActionCount;
    private String depth;
    private int actionsCount;
    private String TargetEntity;
    private  String create;
    private  String mode;

    public void setCreate(String create) {
        this.create = create;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getCreate() {
        return create;
    }

    public String getMode() {
        return mode;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public void setActionsCount(int actionsCount) {
        this.actionsCount = actionsCount;
    }

    public void setTargetEntity(String targetEntity) {
        TargetEntity = targetEntity;
    }

    public String getDepth() {
        return depth;
    }

    public int getActionsCount() {
        return actionsCount;
    }

    public String getTargetEntity() {
        return TargetEntity;
    }

    public String getConditionProperty() {
        return conditionProperty;
    }

    public void setConditionProperty(String conditionProperty) {
        this.conditionProperty = conditionProperty;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    public int getConditionsCount() {
        return conditionsCount;
    }

    public void setConditionsCount(int conditionsCount) {
        this.conditionsCount = conditionsCount;
    }

    public int getThenActionCount() {
        return thenActionCount;
    }

    public void setThenActionCount(int thenActionCount) {
        this.thenActionCount = thenActionCount;
    }

    public int getElseActionCount() {
        return elseActionCount;
    }

    public void setElseActionCount(int elseActionCount) {
        this.elseActionCount = elseActionCount;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getCalcType() {
        return calcType;
    }

    public void setCalcType(String calcType) {
        this.calcType = calcType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String conditionProperty) {
        this.property = conditionProperty;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getMainEntity() {
        return mainEntity;
    }

    public void setMainEntity(String mainEntity) {
        this.mainEntity = mainEntity;
    }

    public String getSecondaryEntity() {
        return secondaryEntity;
    }

    public void setSecondaryEntity(String secondaryEntity) {
        this.secondaryEntity = secondaryEntity;
    }

    @Override
    public String toString() {
        return type;
    }
}
