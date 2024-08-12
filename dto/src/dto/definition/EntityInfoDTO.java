package dto.definition;

import java.util.*;

public class EntityInfoDTO  {
    private String name;
    private String population;
    private List<PropertyInfoDTO> propertyInfoDTOSet;
    private final Map<String, PropertyInfoDTO> propertyInfoDTOMap;

    public EntityInfoDTO() {
        this.propertyInfoDTOMap = new HashMap<>();
    }

    public void setName(String name) {
        this.name = name;
        propertyInfoDTOSet = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setPopulation(Integer population) {
        this.population = population.toString();
    }

    public String getPopulation() {
        return population;
    }

    public List<PropertyInfoDTO> getPropertyInfoDTOSet() {
        return propertyInfoDTOSet;
    }

    public void addProperty(PropertyInfoDTO propertyInfoDTO){
        propertyInfoDTOSet.add(propertyInfoDTO);
    }

    public void setPropertyInfoDTOSet(List<PropertyInfoDTO> propertyInfoDTOSet) {
        this.propertyInfoDTOSet = propertyInfoDTOSet;
        for(PropertyInfoDTO propertyInfoDTO : propertyInfoDTOSet){
            propertyInfoDTOMap.put(propertyInfoDTO.getName(),propertyInfoDTO);
        }
    }

    public PropertyInfoDTO getPropertyInfo(String value) {
        return propertyInfoDTOMap.get(value);
    }
}
