@Test
    public void testDoRead() throws Exception {
        // Arrange
        ProductDefinitionBean formBean = mock(ProductDefinitionBean.class);
        when(formBean.getStatus()).thenReturn("ACTIVE");
        when(formBean.getMakerId()).thenReturn("user123");
        when(formBean.getCheckerId()).thenReturn("checker456");
        when(formBean.getCheckerDate()).thenReturn(null);
        when(formBean.getMakerDate()).thenReturn(null);
        
        AdvancedTestGenerator spyGenerator = spy(advancedTestGenerator);
        doReturn(formBean).when(spyGenerator).getManagedBean(ProductDefinitionBean.BACKING_BEAN_NAME);

        // Act
        String result = spyGenerator.doRead();

        // Assert
        assertNull(result);
        assertEquals("ACTIVE", formBean.getStatus());
        verify(spyGenerator, times(1)).getManagedBean(ProductDefinitionBean.BACKING_BEAN_NAME);
    }
